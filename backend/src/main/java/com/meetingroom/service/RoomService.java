package com.meetingroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meetingroom.dto.RoomDTO;
import com.meetingroom.vo.RoomVO;

import java.util.List;

/**
 * 会议室服务
 */
public interface RoomService {

    /** 分页查询会议室 */
    IPage<RoomVO> list(Integer page, Integer size, String location, Integer capacity);

    /** 获取会议室详情 */
    RoomVO detail(Long roomId);

    /** 新增会议室 */
    void add(RoomDTO dto);

    /** 修改会议室 */
    void update(RoomDTO dto);

    /** 删除会议室 */
    void delete(Long id);

    /** 获取会议室可预约时段列表 */
    List<String> getTimeSlot(Long roomId);
}
