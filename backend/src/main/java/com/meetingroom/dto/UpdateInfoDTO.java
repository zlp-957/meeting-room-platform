package com.meetingroom.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 修改个人信息入参
 */
@Data
public class UpdateInfoDTO {

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    /** 手机号为空表示不修改；填写时必须是 11 位大陆手机号（B-09 修复） */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
