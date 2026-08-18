package com.ifpr.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password
    ) {}

    public record LoginRequest(
        @NotBlank @Email String email, 
        @NotBlank String password
    ) {}

    public record LoginResponse(
        String accessToken, 
        String tokenType, 
        long expiresIn
    ) {}

    public record RegisterResponse(
        Long id, 
        String name, 
        String email, 
        LocalDateTime createdAt
    ) {}

    public record ForgotPasswordRequest(
        @NotBlank @Email String email
    ) {}

    public record ForgotPasswordResponse(
        String message, 
        String debugToken
    ) {}

    public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 100) 
        String newPassword
    ) {}

    public record MessageResponse(
        String message
    ) {}
}
