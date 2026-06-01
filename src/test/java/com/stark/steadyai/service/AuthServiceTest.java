package com.stark.steadyai.service;

import com.stark.steadyai.dto.RegisterRequest;
import com.stark.steadyai.entity.User;
import com.stark.steadyai.enums.Role;
import com.stark.steadyai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void testRegisterUser_AssignsRoleUserAndEncodesPassword() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        authService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPasswordHash());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    void testRegisterUser_ThrowsIfEmailExists() {
        RegisterRequest request = new RegisterRequest(null, "existing@example.com", null);

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.registerUser(request));
    }
}
