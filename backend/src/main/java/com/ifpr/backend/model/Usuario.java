package com.ifpr.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false, length = 100)
    private String senhaCriptografada;

    // Preferência simples do usuário.
    @Column(length = 3)
    private String moedaPadrao = "BRL";

    private Boolean ativo = true;

    private LocalDateTime ultimoAcessoEm;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
        if (moedaPadrao == null || moedaPadrao.isBlank()) moedaPadrao = "BRL";
        if (ativo == null) ativo = true;
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    // Mantidos apenas porque possuem regra além de um getter simples.
    public String getMoedaPadrao() {
        return moedaPadrao == null || moedaPadrao.isBlank() ? "BRL" : moedaPadrao;
    }

    public boolean isAtivo() {
        return ativo == null || ativo;
    }
}
