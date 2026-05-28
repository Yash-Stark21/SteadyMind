package com.stark.steadyai.config;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF: enabled for Thymeleaf form safety, but disabled for H2 console and REST API
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll() // Will be tightened when real auth is added
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
