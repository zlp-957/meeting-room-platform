package com.meetingroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meetingroom.dto.AuditDTO;
import com.meetingroom.dto.BookAddDTO;
import com.meetingroom.vo.BookVO;

import java.util.List;

/**
 * 预约服务
 */
public interface BookService {

    /** 提交预约 */
    void add(BookAddDTO dto);

    /** 审核预约（管理员） */
    void audit(AuditDTO dto);

    /** 取消预约（本人） */
    void cancel(Long bookId);

    /** 用户端：查询本人预约记录 */
    IPage<BookVO> listMine(Integer page, Integer size);

    /** 管理员端：查询全部预约，支持筛选 */
    IPage<BookVO> listAll(Integer page, Integer size, Long roomId, String bookDate, String auditStatus);

    /** 管理员端：删除预约记录 */
    void delete(Long bookId);

    /** 查询某会议室某天已通过的预约占用的时段列表（所有登录用户可用） */
    List<String> occupiedSlots(Long roomId, String bookDate);
}
