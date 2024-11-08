package com.example.user02.config;

import com.example.user02.model.Role;
import com.example.user02.model.User;
import com.example.user02.repository.RoleRepository;
import com.example.user02.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    @Transactional // Đảm bảo thực thi trong một giao dịch
    public CommandLineRunner createAdminUser() {
        return args -> {
            String adminEmail = "hoang@gmail.com";
            String adminPassword = "123456";

            // Kiểm tra xem tài khoản admin đã tồn tại hay chưa
            if (userRepository.findByEmail(adminEmail) == null) {
                // Tạo hoặc lấy vai trò ADMIN từ cơ sở dữ liệu
                Role adminRole = roleRepository.findByName("ROLE_ADMIN");
                if (adminRole == null) {
                    adminRole = new Role();
                    adminRole.setName("ROLE_ADMIN");
                    adminRole = roleRepository.save(adminRole); // Lưu và làm mới đối tượng
                }

                // Tạo tài khoản admin
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRoles(List.of(adminRole)); // Gán vai trò đã được quản lý vào người dùng
                userRepository.save(admin);

                System.out.println("Admin account created with email: " + adminEmail);
            }
        };
    }
}
