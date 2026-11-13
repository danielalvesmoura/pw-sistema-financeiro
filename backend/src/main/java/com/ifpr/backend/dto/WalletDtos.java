package com.ifpr.backend.dto;

import com.ifpr.backend.model.PapelCarteira;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class WalletDtos {
    private WalletDtos() {}

    public record WalletRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "deve possuir 3 letras")
        String currency,
        @DecimalMin("0.00")
        @Digits(integer = 15, fraction = 2)
        BigDecimal initialBalance
    ) {}

    public record WalletResponse(
        Long id,
        String name,
        String description,
        String currency,
        BigDecimal initialBalance,
        Long ownerId,
        String ownerName,
        PapelCarteira role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record AddMemberRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotNull PapelCarteira role
    ) {}

    public record UpdateMemberRoleRequest(
        @NotNull PapelCarteira role
    ) {}

    public record MemberResponse(
        Long userId,
        String name,
        String email,
        PapelCarteira role,
        LocalDateTime joinedAt,
        boolean pendingInvite,
        LocalDateTime inviteExpiresAt
    ) {}
}
