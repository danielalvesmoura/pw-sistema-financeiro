package com.ifpr.backend.repository;

import com.ifpr.backend.model.Categoria;
import com.ifpr.backend.model.TipoTransacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByCarteiraIdOrderByOrdemExibicaoAscNomeAsc(Long carteiraId);
    List<Categoria> findByCarteiraIdAndTipoOrderByOrdemExibicaoAscNomeAsc(Long carteiraId, TipoTransacao tipo);
    List<Categoria> findByUsuarioIdAndCarteiraIsNullOrderByOrdemExibicaoAscNomeAsc(Long usuarioId);
    void deleteByCarteiraId(Long carteiraId);
}
