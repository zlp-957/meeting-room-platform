package com.meetingroom.vo;

import lombok.Data;

import java.util.List;

/**
 * 会议室 VO（时段解析为 List）
 */
@Data
public class RoomVO {

    private Long id;

    private String roomName;

    private String location;

    private Integer capacity;

    private String equipment;

    private List<String> availableTimeSlot;

    private String remark;
}
