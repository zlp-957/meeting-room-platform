package com.meetingroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 预约提交入参
 */
@Data
public class BookAddDTO {

    @NotNull(message = "会议室不能为空")
    private Long roomId;

    @NotBlank(message = "预约标题不能为空")
    @Size(max = 200, message = "预约标题不能超过200个字符")
    private String bookTitle;

    @NotBlank(message = "预约日期不能为空")
    private String bookDate;

    @NotBlank(message = "预约时段不能为空")
    private String timeSlot;

    @NotNull(message = "参会人数不能为空")
    @javax.validation.constraints.Min(value = 1, message = "参会人数至少为1人")
    private Integer peopleNum;
}
