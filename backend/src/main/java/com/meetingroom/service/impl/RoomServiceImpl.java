package com.meetingroom.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meetingroom.common.BusinessException;
import com.meetingroom.dto.RoomDTO;
import com.meetingroom.entity.MeetingBook;
import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.mapper.BookMapper;
import com.meetingroom.mapper.RoomMapper;
import com.meetingroom.service.RoomService;
import com.meetingroom.vo.RoomVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private static final List<String> ACTIVE_STATUS = Arrays.asList("wait", "pass");

    @Resource
    private RoomMapper roomMapper;

    @Resource
    private BookMapper bookMapper;

    @Override
    public IPage<RoomVO> list(Integer page, Integer size, String location, Integer capacity) {
        LambdaQueryWrapper<MeetingRoom> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(location)) {
            wrapper.like(MeetingRoom::getLocation, location);
        }
        if (capacity != null) {
            wrapper.ge(MeetingRoom::getCapacity, capacity);
        }
        wrapper.orderByAsc(MeetingRoom::getId);

        Page<MeetingRoom> p = new Page<>(page, size);
        IPage<MeetingRoom> pageResult = roomMapper.selectPage(p, wrapper);
        return pageResult.convert(this::toVO);
    }

    @Override
    public RoomVO detail(Long roomId) {
        MeetingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }
        return toVO(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RoomDTO dto) {
        MeetingRoom room = new MeetingRoom();
        applyDto(room, dto);
        roomMapper.insert(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoomDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("会议室id不能为空");
        }
        MeetingRoom room = roomMapper.selectById(dto.getId());
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }
        applyDto(room, dto);
        roomMapper.updateById(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 删除前校验是否存在有效预约（wait/pass），有则拒绝，避免孤儿数据（B-07）
        Long activeBooks = bookMapper.selectCount(new LambdaQueryWrapper<MeetingBook>()
                .eq(MeetingBook::getRoomId, id)
                .in(MeetingBook::getAuditStatus, ACTIVE_STATUS));
        if (activeBooks > 0) {
            throw new BusinessException("该会议室存在待审核或已通过的预约，无法删除；请先处理相关预约");
        }
        roomMapper.deleteById(id);
    }

    @Override
    public List<String> getTimeSlot(Long roomId) {
        MeetingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }
        return parseSlots(room.getAvailableTimeSlot());
    }

    private void applyDto(MeetingRoom room, RoomDTO dto) {
        room.setRoomName(dto.getRoomName());
        room.setLocation(dto.getLocation());
        room.setCapacity(dto.getCapacity());
        room.setEquipment(dto.getEquipment());
        room.setRemark(dto.getRemark());
        // 时段列表：null 表示不修改（保留原时段）；传空数组表示清空时段（B-17）
        if (dto.getAvailableTimeSlot() != null) {
            if (CollUtil.isEmpty(dto.getAvailableTimeSlot())) {
                room.setAvailableTimeSlot(null);
            } else {
                room.setAvailableTimeSlot(JSONUtil.toJsonStr(dto.getAvailableTimeSlot()));
            }
        }
    }

    public static List<String> parseSlots(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(json, String.class);
    }

    private RoomVO toVO(MeetingRoom room) {
        RoomVO vo = new RoomVO();
        vo.setId(room.getId());
        vo.setRoomName(room.getRoomName());
        vo.setLocation(room.getLocation());
        vo.setCapacity(room.getCapacity());
        vo.setEquipment(room.getEquipment());
        vo.setRemark(room.getRemark());
        vo.setAvailableTimeSlot(parseSlots(room.getAvailableTimeSlot()));
        return vo;
    }
}
