package com.ifpr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class UserDtos {
    private static final String STRONG_PASSWORD =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";

    private UserDtos() {}

    public record UserResponse(
        Long id,
        String name,
        String email,
        String defaultCurrency,
        LocalDateTime lastAccessAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record UpdateUserRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "deve possuir 3 letras")
        String defaultCurrency
    ) {}

    public record ChangePasswordRequest(
        @NotBlank @Size(max = 100) String currentPassword,
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = STRONG_PASSWORD,
            message = "deve conter letra maiúscula, letra minúscula e número"
        )
        String newPassword
    ) {}
}
