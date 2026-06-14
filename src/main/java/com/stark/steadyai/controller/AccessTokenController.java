package com.stark.steadyai.controller;

import com.stark.steadyai.dto.AccessTokenResponse;
import com.stark.steadyai.security.CustomUserDetails;
import com.stark.steadyai.security.GeneratedJwtToken;
import com.stark.steadyai.security.JwtTokenService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessTokenController {

    private final JwtTokenService jwtTokenService;

    public AccessTokenController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/auth/access-token")
    public ResponseEntity<AccessTokenResponse> accessToken(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        GeneratedJwtToken token = jwtTokenService.generateToken(userDetails);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AccessTokenResponse(token.tokenValue(), "Bearer", token.expiresAt()));
    }
}
