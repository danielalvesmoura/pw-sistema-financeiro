package com.ifpr.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tokens_redefinicao_senha", uniqueConstraints = @UniqueConstraint(columnNames = "token"))
public class TokenRedefinicaoSenha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 80)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private boolean utilizado;

    @Column(length = 64)
    private String ipOrigem;

    @Column(length = 40)
    private String tipoSolicitacao = "RECUPERACAO_SENHA";

    private LocalDateTime criadoEm;

    private LocalDateTime utilizadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (tipoSolicitacao == null || tipoSolicitacao.isBlank()) tipoSolicitacao = "RECUPERACAO_SENHA";
    }
}
