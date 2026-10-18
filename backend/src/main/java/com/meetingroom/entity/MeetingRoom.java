package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会议室实体
 */
@Data
@TableName("meeting_room")
public class MeetingRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomName;

    private String location;

    private Integer capacity;

    private String equipment;

    /** 可预约时段，JSON 数组字符串 */
    private String availableTimeSlot;

    private String remark;
}
