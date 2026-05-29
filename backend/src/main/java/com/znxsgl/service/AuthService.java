package com.znxsgl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.znxsgl.config.JwtUtil;
import com.znxsgl.dto.LoginRequest;
import com.znxsgl.dto.LoginResponse;
import com.znxsgl.entity.User;
import com.znxsgl.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        // 用 username 查找用户（学号/用户名均可）
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getStudentNo, request.getUsername()));
        }

        if (user == null) {
            return new LoginResponse("账号不存在");
        }
        if (user.getStatus() != 1) {
            return new LoginResponse("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new LoginResponse("密码错误");
        }

        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return LoginResponse.success(token, user.getRealName(), user.getUsername(),
                user.getRole(), user.getId(), user.getAvatarUrl());
    }
}
