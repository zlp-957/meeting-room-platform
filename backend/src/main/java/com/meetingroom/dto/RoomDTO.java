package com.meetingroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 会议室新增/修改入参
 */
@Data
public class RoomDTO {

    private Long id;

    @NotBlank(message = "会议室名称不能为空")
    private String roomName;

    private String location;

    @NotNull(message = "容纳人数不能为空")
    private Integer capacity;

    private String equipment;

    /** 可预约时段列表 */
    @Size(min = 1, message = "至少配置一个可预约时段")
    private List<String> availableTimeSlot;

    private String remark;
}
