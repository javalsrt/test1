package com.znxsgl.config;

import com.znxsgl.entity.User;
import com.znxsgl.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("===== 数据库已有用户，跳过初始化 =====");
            return;
        }

        String pwd = passwordEncoder.encode("123456");
        System.out.println("===== 生成的密码哈希: " + pwd + " =====");

        // 创建教师
        User teacher1 = new User();
        teacher1.setUsername("teacher1");
        teacher1.setPasswordHash(pwd);
        teacher1.setRealName("张明远");
        teacher1.setRole(2);
        teacher1.setEmail("zhangmy@university.edu.cn");
        teacher1.setStatus(1);
        userRepository.save(teacher1);

        User teacher2 = new User();
        teacher2.setUsername("teacher2");
        teacher2.setPasswordHash(pwd);
        teacher2.setRealName("李伟强");
        teacher2.setRole(2);
        teacher2.setEmail("liwq@university.edu.cn");
        teacher2.setStatus(1);
        userRepository.save(teacher2);

        // 创建管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(pwd);
        admin.setRealName("系统管理员");
        admin.setRole(3);
        admin.setStatus(1);
        userRepository.save(admin);

        // 创建学生
        User student1 = new User();
        student1.setStudentNo("20240101001");
        student1.setUsername("student1");
        student1.setPasswordHash(pwd);
        student1.setRealName("张明");
        student1.setRole(1);
        student1.setStatus(1);
        userRepository.save(student1);

        User student2 = new User();
        student2.setStudentNo("20240101002");
        student2.setUsername("student2");
        student2.setPasswordHash(pwd);
        student2.setRealName("李婷");
        student2.setRole(1);
        student2.setStatus(1);
        userRepository.save(student2);

        System.out.println("===== 默认用户创建完成（密码均为 123456） =====");
    }
}
