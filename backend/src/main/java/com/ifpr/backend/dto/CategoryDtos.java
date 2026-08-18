package com.ifpr.backend.dto;

import com.ifpr.backend.model.TipoTransacao;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CategoryDtos {
    private CategoryDtos() {}

    public record CategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull TipoTransacao type,
        @Size(max = 80) String icon,
        @Size(max = 255) String description,
        @Min(0) Integer displayOrder,
        Boolean active
    ) {}

    public record CategoryResponse(
        Long id,
        Long walletId,
        String name,
        TipoTransacao type,
        String icon,
        String description,
        int displayOrder,
        boolean active
    ) {}
}
