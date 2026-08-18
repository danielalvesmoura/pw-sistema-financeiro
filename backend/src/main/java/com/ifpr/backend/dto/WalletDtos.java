package com.ifpr.backend.dto;

import com.ifpr.backend.model.PapelCarteira;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class WalletDtos {
    private WalletDtos() {}

    public record WalletRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @Size(min = 3, max = 3) String currency,
        @DecimalMin("0.00") BigDecimal initialBalance,
        Boolean archived
    ) {}

    public record WalletResponse(
        Long id,
        String name,
        String description,
        String currency,
        BigDecimal initialBalance,
        boolean archived,
        Long ownerId,
        String ownerName,
        PapelCarteira role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record AddMemberRequest(
        @NotBlank @Email String email, 
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
        LocalDateTime inviteExpiresAt,
        boolean active
    ) {}
}
