package com.stark.steadyai.dto;

import com.stark.steadyai.entity.User;

public record AuthenticatedUserResponse(
        Long id,
        String name,
        String email,
        String role
) {
    public static AuthenticatedUserResponse from(User user) {
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
