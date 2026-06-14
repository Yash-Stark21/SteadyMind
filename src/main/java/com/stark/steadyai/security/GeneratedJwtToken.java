package com.stark.steadyai.security;

import java.time.Instant;

public record GeneratedJwtToken(
        String tokenValue,
        Instant expiresAt
) {
}
