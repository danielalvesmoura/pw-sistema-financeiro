package com.ifpr.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mantido para registrar quem criou a categoria.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Categorias novas pertencem a uma carteira e ficam disponíveis para seus membros.
    // nullable=true preserva compatibilidade com categorias criadas em versões antigas.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id")
    private Carteira carteira;

    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransacao tipo;

    @Column(length = 80)
    private String icone;

    private Integer ordemExibicao = 0;

    private Boolean ativo = true;

    @Column(length = 255)
    private String descricao;

    public int getOrdemExibicao() {
        return ordemExibicao == null ? 0 : ordemExibicao;
    }

    public boolean isAtivo() {
        return ativo == null || ativo;
    }
}
