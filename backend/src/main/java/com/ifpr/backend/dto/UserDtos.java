package com.ifpr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class UserDtos {
    private UserDtos() {}

    public record UserResponse(
        Long id,
        String name,
        String email,
        String defaultCurrency,
        boolean active,
        LocalDateTime lastAccessAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record UpdateUserRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(min = 3, max = 3) String defaultCurrency
    ) {}

    public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 100) String newPassword
    ) {}
}
