package com.meetingroom.controller;

import com.meetingroom.common.PageUtils;
import com.meetingroom.common.Result;
import com.meetingroom.dto.LoginDTO;
import com.meetingroom.dto.RegisterDTO;
import com.meetingroom.dto.ResetPasswordDTO;
import com.meetingroom.dto.UpdateInfoDTO;
import com.meetingroom.dto.UpdatePasswordDTO;
import com.meetingroom.dto.UserAdminUpdateDTO;
import com.meetingroom.service.UserService;
import com.meetingroom.vo.LoginVO;
import com.meetingroom.vo.UserVO;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "找回密码")
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.success(userService.getInfo());
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/updateInfo")
    public Result<Void> updateInfo(@Valid @RequestBody UpdateInfoDTO dto) {
        userService.updateInfo(dto);
        return Result.success();
    }

    @SaCheckRole("admin")
    @Operation(summary = "用户列表（管理员）")
    @GetMapping("/list")
    public Result<IPage<UserVO>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String username) {
        return Result.success(userService.listUsers(PageUtils.normalizePage(page), PageUtils.normalizeSize(size), username));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/updatePassword")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }

    @SaCheckRole("admin")
    @Operation(summary = "修改用户信息（管理员，含禁用/启用）")
    @PutMapping("/admin/update/{userId}")
    public Result<Void> adminUpdateUser(@PathVariable Long userId, @Valid @RequestBody UserAdminUpdateDTO dto) {
        userService.adminUpdateUser(userId, dto);
        return Result.success();
    }

    @SaCheckRole("admin")
    @Operation(summary = "删除用户（管理员）")
    @DeleteMapping("/admin/delete/{userId}")
    public Result<Void> adminDeleteUser(@PathVariable Long userId) {
        userService.adminDeleteUser(userId);
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }
}
