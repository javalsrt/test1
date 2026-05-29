package com.znxsgl.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.znxsgl.entity.User;
import com.znxsgl.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder, JdbcTemplate jdbc) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        List<User> users = userMapper.selectList(null);

        if (users.isEmpty()) {
            System.out.println("===== 用户表为空，请先执行 seed_all.sql ======");
            return;
        }

        int updated = 0;
        for (User u : users) {
            String hash = u.getPasswordHash();
            if (hash != null && hash.length() < 20) {
                u.setPasswordHash(passwordEncoder.encode(hash));
                userMapper.updateById(u);
                updated++;
                System.out.println("  密码加密: " + u.getUsername());
            }
        }

        if (updated > 0) {
            System.out.println("===== 已加密 " + updated + " 个用户密码（密码均为 123456）=====");
        } else {
            System.out.println("===== 所有密码均已加密，跳过 =====");
        }

        // 修复课程表：给 credit 为 NULL 或 0 的课程设置合理课时
        // 默认值按课程类型：必修=4, 限选=2, 公共课=2
        jdbc.update("UPDATE course SET credit = 4 WHERE (credit IS NULL OR credit = 0) AND course_type = '必修'");
        jdbc.update("UPDATE course SET credit = 2 WHERE (credit IS NULL OR credit = 0) AND course_type IN ('限选','选修')");
        jdbc.update("UPDATE course SET credit = 2 WHERE (credit IS NULL OR credit = 0)");
        int fixed = jdbc.queryForObject("SELECT COUNT(*) FROM course WHERE credit IS NULL OR credit = 0", Integer.class);
        if (fixed > 0) {
            System.out.println("===== 仍有 " + fixed + " 个课程课时为0 =====");
        }
        System.out.println("===== 课程课时已检查 =====");
    }
}
