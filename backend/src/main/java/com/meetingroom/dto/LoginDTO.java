package com.meetingroom.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录入参
 */
@Data
public class LoginDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
