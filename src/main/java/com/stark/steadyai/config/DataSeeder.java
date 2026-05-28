package com.stark.steadyai.config;

import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.Role;
import com.stark.steadyai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("user@example.com").isEmpty()) {
                User user = new User("Test User", "user@example.com", passwordEncoder.encode("password123"));
                user.setRole(Role.ROLE_USER);
                userRepository.save(user);
            }

            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User("Admin User", "admin@example.com", passwordEncoder.encode("password123"));
                admin.setRole(Role.ROLE_ADMIN);
                userRepository.save(admin);
            }

            if (userRepository.findByEmail("therapist@example.com").isEmpty()) {
                User therapist = new User("Therapist User", "therapist@example.com", passwordEncoder.encode("password123"));
                therapist.setRole(Role.ROLE_THERAPIST);
                userRepository.save(therapist);
            }
            
            if (userRepository.findByEmail("demo@steadyai.local").isEmpty()) {
                User demoUser = new User("Demo User", "demo@steadyai.local", passwordEncoder.encode("password123"));
                demoUser.setRole(Role.ROLE_USER);
                userRepository.save(demoUser);
            }
        };
    }
}
