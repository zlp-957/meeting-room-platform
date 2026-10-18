package com.meetingroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meetingroom.dto.LoginDTO;
import com.meetingroom.dto.RegisterDTO;
import com.meetingroom.dto.ResetPasswordDTO;
import com.meetingroom.dto.UpdateInfoDTO;
import com.meetingroom.dto.UpdatePasswordDTO;
import com.meetingroom.dto.UserAdminUpdateDTO;
import com.meetingroom.vo.LoginVO;
import com.meetingroom.vo.UserVO;

/**
 * 用户服务
 */
public interface UserService {

    /** 注册 */
    void register(RegisterDTO dto);

    /** 登录 */
    LoginVO login(LoginDTO dto);

    /** 退出登录，使当前 Token 失效 */
    void logout();

    /** 获取当前登录用户信息 */
    UserVO getInfo();

    /** 修改个人信息 */
    void updateInfo(UpdateInfoDTO dto);

    /** 修改密码 */
    void updatePassword(UpdatePasswordDTO dto);

    /** 找回密码：核对账号+真实姓名+手机号后重置密码 */
    void resetPassword(ResetPasswordDTO dto);

    /** 管理员：分页查询用户列表 */
    IPage<UserVO> listUsers(Integer page, Integer size, String username);

    /** 管理员：修改用户信息（含禁用/启用） */
    void adminUpdateUser(Long userId, UserAdminUpdateDTO dto);

    /** 管理员：删除用户 */
    void adminDeleteUser(Long userId);
}
