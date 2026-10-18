package com.meetingroom.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meetingroom.common.BusinessException;
import com.meetingroom.dto.LoginDTO;
import com.meetingroom.dto.RegisterDTO;
import com.meetingroom.dto.ResetPasswordDTO;
import com.meetingroom.dto.UpdateInfoDTO;
import com.meetingroom.dto.UpdatePasswordDTO;
import com.meetingroom.dto.UserAdminUpdateDTO;
import com.meetingroom.entity.User;
import com.meetingroom.mapper.UserMapper;
import com.meetingroom.service.UserService;
import com.meetingroom.vo.LoginVO;
import com.meetingroom.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserServiceImpl implements UserService {

    /** 登录失败锁定阈值与时长（B-11） */
    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_MILLIS = 10 * 60 * 1000L;

    /** 简单单机失败计数器：username -> [失败次数, 首次失败时间] */
    private final ConcurrentHashMap<String, FailRecord> failCounter = new ConcurrentHashMap<>();

    @Resource
    private UserMapper userMapper;

    private static class FailRecord {
        final AtomicInteger count = new AtomicInteger();
        volatile long firstFailAt = System.currentTimeMillis();

        boolean expired() {
            return System.currentTimeMillis() - firstFailAt > LOCK_MILLIS;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 账号唯一校验
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("该账号已存在");
        }
        // 手机号唯一校验：一个手机号只能注册一个账号
        Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()));
        if (phoneCount > 0) {
            throw new BusinessException("该手机号已被注册，请更换手机号");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        // 密码 BCrypt 加密
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole("user");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername();
        // 登录失败次数限制（B-11）：连续失败达到阈值后锁定一段时间
        FailRecord record = failCounter.get(username);
        if (record != null) {
            if (record.expired()) {
                failCounter.remove(username);
            } else if (record.count.get() >= MAX_FAIL_COUNT) {
                throw new BusinessException("登录失败次数过多，账号已临时锁定，请10分钟后重试");
            }
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .or()
                .eq(User::getPhone, username));
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            recordLoginFail(username);
            throw new BusinessException("账号或密码错误");
        }
        // 账号被禁用时禁止登录
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        // 登录成功清除失败计数
        failCounter.remove(username);
        StpUtil.login(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUser(toVO(user));
        return vo;
    }

    private void recordLoginFail(String username) {
        FailRecord record = failCounter.get(username);
        if (record == null) {
            record = new FailRecord();
            FailRecord old = failCounter.putIfAbsent(username, record);
            if (old != null) {
                record = old;
            }
        }
        record.count.incrementAndGet();
    }

    @Override
    public void logout() {
        // 主动登出，使当前 Token 立即失效（B-10）
        StpUtil.logout();
    }

    @Override
    public UserVO getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(UpdateInfoDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 手机号唯一校验（排除自己），撞库时给出准确提示
        if (StrUtil.isNotBlank(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, dto.getPhone())
                    .ne(User::getId, userId));
            if (phoneCount > 0) {
                throw new BusinessException("该手机号已被其他账号使用，请更换手机号");
            }
        }
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        userMapper.updateById(user);
    }

    @Override
    public IPage<UserVO> listUsers(Integer page, Integer size, String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByAsc(User::getId);
        Page<User> p = new Page<>(page, size);
        return userMapper.selectPage(p, wrapper).convert(this::toVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 校验原密码
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        // 核对账号是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        // 核对真实姓名与手机号是否匹配
        if (!dto.getRealName().equals(user.getRealName())
                || !dto.getPhone().equals(user.getPhone())) {
            throw new BusinessException("实名信息不匹配，无法找回密码");
        }
        // 重置密码（允许与原密码相同）
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(user);
        // 找回密码后强制下线，需重新登录
        StpUtil.logout(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateUser(Long userId, UserAdminUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 不允许管理员禁用/删除自己
        if (userId.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("不能对自己执行该操作");
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            // 手机号唯一校验（排除该用户自己）
            if (StrUtil.isNotBlank(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
                Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, dto.getPhone())
                        .ne(User::getId, userId));
                if (phoneCount > 0) {
                    throw new BusinessException("该手机号已被其他账号使用，请更换手机号");
                }
            }
            user.setPhone(dto.getPhone());
        }
        if (dto.getStatus() != null) {
            if (dto.getStatus() != 0 && dto.getStatus() != 1) {
                throw new BusinessException("非法的状态值");
            }
            user.setStatus(dto.getStatus());
        }
        userMapper.updateById(user);
        // 禁用后立即踢下线，使该用户已有 Token 失效
        if (dto.getStatus() != null && dto.getStatus() == 0) {
            StpUtil.logout(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (userId.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("不能删除自己");
        }
        userMapper.deleteById(userId);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
