package com.znxsgl.service;

import com.znxsgl.config.JwtUtil;
import com.znxsgl.dto.LoginRequest;
import com.znxsgl.dto.LoginResponse;
import com.znxsgl.entity.User;
import com.znxsgl.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        // 用 username 查找用户（学号/用户名均可）
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByStudentNo(request.getUsername())
                        .orElse(null));

        if (user == null) {
            return new LoginResponse("账号不存在");
        }

        if (user.getStatus() != 1) {
            return new LoginResponse("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new LoginResponse("密码错误");
        }

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 生成 token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.success(
                token, user.getRealName(), user.getUsername(),
                user.getRole(), user.getId(), user.getAvatarUrl()
        );
    }
}
