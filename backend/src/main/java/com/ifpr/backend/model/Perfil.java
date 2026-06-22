package com.ifpr.backend.model;

import org.hibernate.validator.constraints.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "(descricao.obrigatorio)")
    private String descricao;
}
