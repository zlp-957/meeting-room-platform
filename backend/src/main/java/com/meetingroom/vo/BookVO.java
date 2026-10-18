package com.meetingroom.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约记录 VO（含关联的用户名、会议室名）
 */
@Data
public class BookVO {

    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private Long roomId;

    private String roomName;

    private String bookTitle;

    private String timeSlot;

    private LocalDate bookDate;

    private Integer peopleNum;

    private String realName;

    private String phone;

    private String auditStatus;

    private String rejectReason;

    private LocalDateTime createTime;
}
