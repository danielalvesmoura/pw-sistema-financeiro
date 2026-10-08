package com.ifpr.backend.repository;

import com.ifpr.backend.model.Transacao;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {
    boolean existsByCategoriaId(Long categoriaId);
    List<Transacao> findByCarteiraId(Long carteiraId);

    @EntityGraph(attributePaths = {"categoria", "categoria.usuario", "criadoPor"})
    List<Transacao> findByCarteiraIdAndDataBetweenOrderByDataAscIdAsc(
        Long carteiraId,
        LocalDate startDate,
        LocalDate endDate
    );
    void deleteByCarteiraId(Long carteiraId);
}
