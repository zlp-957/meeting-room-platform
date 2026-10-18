package com.meetingroom.vo;

import lombok.Data;

/**
 * 用户信息 VO（不含密码）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String realName;

    private String phone;

    private String role;

    /** 账号状态：1 正常 / 0 禁用 */
    private Integer status;

    private java.time.LocalDateTime createTime;
}
