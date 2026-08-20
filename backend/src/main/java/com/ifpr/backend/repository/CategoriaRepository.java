package com.ifpr.backend.repository;

import com.ifpr.backend.model.Categoria;
import com.ifpr.backend.model.TipoTransacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByCarteiraIdAndUsuarioIdOrderByOrdemExibicaoAscNomeAsc(Long carteiraId, Long usuarioId);
    List<Categoria> findByCarteiraIdAndUsuarioIdAndTipoOrderByOrdemExibicaoAscNomeAsc(Long carteiraId, Long usuarioId, TipoTransacao tipo);
    Optional<Categoria> findByIdAndCarteiraIdAndUsuarioId(Long id, Long carteiraId, Long usuarioId);
    List<Categoria> findByUsuarioIdAndCarteiraIsNullOrderByOrdemExibicaoAscNomeAsc(Long usuarioId);
    void deleteByCarteiraId(Long carteiraId);
}
