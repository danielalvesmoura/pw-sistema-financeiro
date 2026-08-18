package com.ifpr.backend.dto;

import com.ifpr.backend.model.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TransactionDtos {
    private TransactionDtos() {}

    public record TransactionRequest(
        @NotNull TipoTransacao type,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Size(max = 255) String description,
        @NotNull @PastOrPresent LocalDate date,
        Long categoryId,
        @Size(max = 500) String attachmentUrl,
        @Size(max = 1000) String notes,
        Boolean recurring,
        @Size(max = 40) String paymentMethod
    ) {}

    public record TransactionResponse(
        Long id,
        Long walletId,
        Long categoryId,
        String categoryName,
        Long createdById,
        String createdByName,
        TipoTransacao type,
        BigDecimal amount,
        String description,
        LocalDate date,
        String attachmentUrl,
        String notes,
        boolean recurring,
        String paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}
