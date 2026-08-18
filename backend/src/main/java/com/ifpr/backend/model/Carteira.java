package com.ifpr.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "carteiras")
public class Carteira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dono_id", nullable = false)
    private Usuario dono;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(length = 3)
    private String moeda = "BRL";

    @Column(precision = 15, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    private Boolean arquivada = false;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
        if (moeda == null || moeda.isBlank()) moeda = "BRL";
        if (saldoInicial == null) saldoInicial = BigDecimal.ZERO;
        if (arquivada == null) arquivada = false;
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public String getMoeda() {
        return moeda == null || moeda.isBlank() ? "BRL" : moeda;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial == null ? BigDecimal.ZERO : saldoInicial;
    }

    public boolean isArquivada() {
        return Boolean.TRUE.equals(arquivada);
    }
}
