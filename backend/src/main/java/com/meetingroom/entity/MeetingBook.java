package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约实体
 */
@Data
@TableName("meeting_book")
public class MeetingBook {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roomId;

    private String bookTitle;

    /** 预约时段，如 08:00-08:45 */
    private String timeSlot;

    /** 预约日期 */
    private LocalDate bookDate;

    private Integer peopleNum;

    /** 预约人真实姓名（实名认证） */
    private String realName;

    /** 预约人手机号（实名认证） */
    private String phone;

    /** wait 待审核 / pass 通过 / reject 驳回 / cancel 取消 */
    private String auditStatus;

    /**
     * 有效预约标志：wait/pass 时为 "1"，reject/cancel 时置 NULL。
     * 配合唯一索引 uk_active_slot(room_id, book_date, time_slot, slot_flag)
     * 在数据库层面保证同一会议室同一时段至多存在一条有效预约（B-01 修复）。
     * MySQL 唯一索引对 NULL 不去重，因此取消/驳回的记录不会阻碍重新预约。
     */
    private String slotFlag;

    private String rejectReason;

    private LocalDateTime createTime;
}
