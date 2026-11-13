package com.ifpr.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class AuthDtos {
    private static final String STRONG_PASSWORD =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";

    private AuthDtos() {}

    public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = STRONG_PASSWORD,
            message = "deve conter letra maiúscula, letra minúscula e número"
        )
        String password
    ) {}

    public record LoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String password
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
        @NotBlank @Email @Size(max = 254) String email
    ) {}

    public record ForgotPasswordResponse(
        String message,
        String debugToken
    ) {}

    public record ResetPasswordRequest(
        @NotBlank @Size(max = 100) String token,
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = STRONG_PASSWORD,
            message = "deve conter letra maiúscula, letra minúscula e número"
        )
        String newPassword
    ) {}

    public record MessageResponse(
        String message
    ) {}
}
