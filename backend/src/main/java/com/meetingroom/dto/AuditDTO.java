package com.meetingroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 预约审核入参
 */
@Data
public class AuditDTO {

    @NotNull(message = "预约单id不能为空")
    private Long bookId;

    /** pass 通过 / reject 驳回 */
    @NotBlank(message = "审核状态不能为空")
    private String auditStatus;

    private String rejectReason;
}
