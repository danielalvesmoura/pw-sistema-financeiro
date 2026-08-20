package com.ifpr.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "carteira_membros", uniqueConstraints = @UniqueConstraint(columnNames = {"carteira_id", "usuario_id"}))
public class CarteiraMembro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id", nullable = false)
    private Carteira carteira;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelCarteira papel;

    @Column(nullable = false)
    private LocalDateTime entradoEm;

    private Boolean convitePendente = false;

    private LocalDateTime conviteExpiraEm;

    private Boolean ativo = true;

    @PrePersist
    void prePersist() {
        entradoEm = LocalDateTime.now();
        if (convitePendente == null) convitePendente = false;
        if (ativo == null) ativo = true;
    }

    public boolean isConvitePendente() {
        return Boolean.TRUE.equals(convitePendente);
    }

    public boolean isAtivo() {
        return ativo == null || ativo;
    }
}
