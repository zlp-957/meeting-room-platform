package com.meetingroom.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meetingroom.common.BusinessException;
import com.meetingroom.dto.AuditDTO;
import com.meetingroom.dto.BookAddDTO;
import com.meetingroom.entity.MeetingBook;
import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.entity.User;
import com.meetingroom.mapper.BookMapper;
import com.meetingroom.mapper.RoomMapper;
import com.meetingroom.mapper.UserMapper;
import com.meetingroom.service.BookService;
import com.meetingroom.vo.BookVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    /** 有效预约状态集合：待审核与已通过均占用时段（B-06 修复） */
    private static final List<String> ACTIVE_STATUS = java.util.Arrays.asList("wait", "pass");

    @Resource
    private BookMapper bookMapper;

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(BookAddDTO dto) {
        LocalDate bookDate;
        try {
            bookDate = LocalDate.parse(dto.getBookDate());
        } catch (Exception e) {
            throw new BusinessException("预约日期格式不正确");
        }

        // ① 校验预约日期不能是过去日期
        if (bookDate.isBefore(LocalDate.now())) {
            throw new BusinessException("不能选择已经过去的日期");
        }

        // ② 查询会议室，校验时段是否属于预设时段
        MeetingRoom room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }
        List<String> slots = RoomServiceImpl.parseSlots(room.getAvailableTimeSlot());
        if (!slots.contains(dto.getTimeSlot())) {
            throw new BusinessException("所选时段不在该会议室可预约时段范围内");
        }

        // ③ 校验参会人数不超过容量
        if (dto.getPeopleNum() != null && room.getCapacity() != null
                && dto.getPeopleNum() > room.getCapacity()) {
            throw new BusinessException("参会人数不能超过会议室容纳人数(" + room.getCapacity() + "人)");
        }

        // ④ 冲突校验：同会议室 + 同日期 + 同时段 + 有效状态(wait/pass)（B-06：待审核同样占用时段）
        Long conflict = bookMapper.selectCount(new LambdaQueryWrapper<MeetingBook>()
                .eq(MeetingBook::getRoomId, dto.getRoomId())
                .eq(MeetingBook::getBookDate, bookDate)
                .eq(MeetingBook::getTimeSlot, dto.getTimeSlot())
                .in(MeetingBook::getAuditStatus, ACTIVE_STATUS));
        if (conflict > 0) {
            throw new BusinessException("该时段已被预约或正在审核中，请选择其他时段");
        }

        // ⑤ 新增预约，状态 wait；数据库唯一索引兜底并发场景（B-01）
        // 实名信息从登录用户自动带出，预约订单与账号绑定，无需用户重复填写
        Long loginUserId = StpUtil.getLoginIdAsLong();
        User loginUser = userMapper.selectById(loginUserId);
        MeetingBook book = new MeetingBook();
        book.setUserId(loginUserId);
        book.setRoomId(dto.getRoomId());
        book.setBookTitle(dto.getBookTitle());
        book.setTimeSlot(dto.getTimeSlot());
        book.setBookDate(bookDate);
        book.setPeopleNum(dto.getPeopleNum());
        book.setRealName(loginUser != null ? loginUser.getRealName() : null);
        book.setPhone(loginUser != null ? loginUser.getPhone() : null);
        book.setAuditStatus("wait");
        book.setSlotFlag("1");
        try {
            bookMapper.insert(book);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该时段已被预约，请选择其他时段");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AuditDTO dto) {
        MeetingBook book = bookMapper.selectById(dto.getBookId());
        if (book == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!"wait".equals(book.getAuditStatus())) {
            throw new BusinessException("该预约已处理，无法重复审核");
        }
        if ("pass".equals(dto.getAuditStatus())) {
            // 审核通过前再次做冲突校验
            Long conflict = bookMapper.selectCount(new LambdaQueryWrapper<MeetingBook>()
                    .eq(MeetingBook::getRoomId, book.getRoomId())
                    .eq(MeetingBook::getBookDate, book.getBookDate())
                    .eq(MeetingBook::getTimeSlot, book.getTimeSlot())
                    .in(MeetingBook::getAuditStatus, ACTIVE_STATUS)
                    .ne(MeetingBook::getId, book.getId()));
            if (conflict > 0) {
                throw new BusinessException("该时段已被其他预约占用，无法通过");
            }
            // 乐观更新：仅当仍是 wait 时才置为 pass，防止并发审核竞态（B-01）
            int updated = bookMapper.update(null, new LambdaUpdateWrapper<MeetingBook>()
                    .eq(MeetingBook::getId, book.getId())
                    .eq(MeetingBook::getAuditStatus, "wait")
                    .set(MeetingBook::getAuditStatus, "pass")
                    .set(MeetingBook::getSlotFlag, "1"));
            if (updated == 0) {
                throw new BusinessException("该预约已被处理，请刷新后查看");
            }
            return;
        } else if ("reject".equals(dto.getAuditStatus())) {
            if (StrUtil.isBlank(dto.getRejectReason())) {
                throw new BusinessException("驳回时需填写驳回原因");
            }
            // 驳回释放时段：slot_flag 置 NULL
            int updated = bookMapper.update(null, new LambdaUpdateWrapper<MeetingBook>()
                    .eq(MeetingBook::getId, book.getId())
                    .eq(MeetingBook::getAuditStatus, "wait")
                    .set(MeetingBook::getAuditStatus, "reject")
                    .set(MeetingBook::getRejectReason, dto.getRejectReason())
                    .set(MeetingBook::getSlotFlag, null));
            if (updated == 0) {
                throw new BusinessException("该预约已被处理，请刷新后查看");
            }
            return;
        }
        throw new BusinessException("非法的审核状态");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long bookId) {
        MeetingBook book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("预约记录不存在");
        }
        // 必须是预约本人
        if (!book.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("只能取消自己的预约");
        }
        // 待审核、已通过才可取消
        if (!"wait".equals(book.getAuditStatus()) && !"pass".equals(book.getAuditStatus())) {
            throw new BusinessException("当前状态不可取消");
        }
        // 已结束的会议不允许取消（B-16）
        if (isExpired(book)) {
            throw new BusinessException("该预约的会议时间已过，无法取消");
        }
        // 取消释放时段：slot_flag 置 NULL
        bookMapper.update(null, new LambdaUpdateWrapper<MeetingBook>()
                .eq(MeetingBook::getId, book.getId())
                .set(MeetingBook::getAuditStatus, "cancel")
                .set(MeetingBook::getSlotFlag, null));
    }

    /** 判断预约的日期时段是否已过（当天且时段开始时间早于当前时间，或日期在过去） */
    private boolean isExpired(MeetingBook book) {
        LocalDate bookDate = book.getBookDate();
        if (bookDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (bookDate.isBefore(today)) {
            return true;
        }
        if (!bookDate.isEqual(today)) {
            return false;
        }
        // 当天：取时段起始时间与当前时间比较
        try {
            String slot = book.getTimeSlot();
            if (StrUtil.isNotBlank(slot) && slot.contains("-")) {
                String start = slot.split("-")[0].trim();
                java.time.LocalTime startTime = java.time.LocalTime.parse(start);
                return startTime.isBefore(java.time.LocalTime.now());
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public IPage<BookVO> listMine(Integer page, Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<MeetingBook> wrapper = new LambdaQueryWrapper<MeetingBook>()
                .eq(MeetingBook::getUserId, userId)
                .orderByDesc(MeetingBook::getCreateTime);
        Page<MeetingBook> p = new Page<>(page, size);
        return convertPage(bookMapper.selectPage(p, wrapper));
    }

    @Override
    public IPage<BookVO> listAll(Integer page, Integer size, Long roomId, String bookDate, String auditStatus) {
        LambdaQueryWrapper<MeetingBook> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(MeetingBook::getRoomId, roomId);
        }
        if (StrUtil.isNotBlank(bookDate)) {
            wrapper.eq(MeetingBook::getBookDate, bookDate);
        }
        if (StrUtil.isNotBlank(auditStatus)) {
            wrapper.eq(MeetingBook::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(MeetingBook::getCreateTime);
        Page<MeetingBook> p = new Page<>(page, size);
        return convertPage(bookMapper.selectPage(p, wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long bookId) {
        bookMapper.deleteById(bookId);
    }

    @Override
    public List<String> occupiedSlots(Long roomId, String bookDate) {
        LocalDate date;
        try {
            date = LocalDate.parse(bookDate);
        } catch (Exception e) {
            throw new BusinessException("日期格式不正确");
        }
        // wait 与 pass 均视为占用（B-06）
        List<MeetingBook> list = bookMapper.selectList(new LambdaQueryWrapper<MeetingBook>()
                .eq(MeetingBook::getRoomId, roomId)
                .eq(MeetingBook::getBookDate, date)
                .in(MeetingBook::getAuditStatus, ACTIVE_STATUS)
                .select(MeetingBook::getTimeSlot));
        List<String> slots = new ArrayList<>();
        for (MeetingBook book : list) {
            slots.add(book.getTimeSlot());
        }
        return slots;
    }

    /** 分页结果整体批量装配用户与会议室信息，消除 N+1（B-02） */
    private IPage<BookVO> convertPage(IPage<MeetingBook> pageResult) {
        List<MeetingBook> records = pageResult.getRecords();
        if (records.isEmpty()) {
            return pageResult.convert(b -> toVO(b, null, null));
        }
        Set<Long> userIds = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        for (MeetingBook b : records) {
            if (b.getUserId() != null) {
                userIds.add(b.getUserId());
            }
            if (b.getRoomId() != null) {
                roomIds.add(b.getRoomId());
            }
        }
        Map<Long, User> users = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, MeetingRoom> rooms = roomIds.isEmpty() ? Collections.emptyMap()
                : roomMapper.selectBatchIds(roomIds).stream()
                        .collect(Collectors.toMap(MeetingRoom::getId, Function.identity()));
        return pageResult.convert(b -> toVO(b, users.get(b.getUserId()), rooms.get(b.getRoomId())));
    }

    private BookVO toVO(MeetingBook book, User user, MeetingRoom room) {
        BookVO vo = new BookVO();
        vo.setId(book.getId());
        vo.setUserId(book.getUserId());
        vo.setRoomId(book.getRoomId());
        vo.setBookTitle(book.getBookTitle());
        vo.setTimeSlot(book.getTimeSlot());
        vo.setBookDate(book.getBookDate());
        vo.setPeopleNum(book.getPeopleNum());
        vo.setRealName(book.getRealName());
        vo.setPhone(book.getPhone());
        vo.setAuditStatus(book.getAuditStatus());
        vo.setRejectReason(book.getRejectReason());
        vo.setCreateTime(book.getCreateTime());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        if (room != null) {
            vo.setRoomName(room.getRoomName());
        }
        return vo;
    }
}
