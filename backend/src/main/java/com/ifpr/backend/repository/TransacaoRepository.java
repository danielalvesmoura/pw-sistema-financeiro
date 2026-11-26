package com.ifpr.backend.repository;

import com.ifpr.backend.model.Transacao;
import com.ifpr.backend.model.TipoTransacao;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("select coalesce(sum(t.valor), 0) from Transacao t where t.carteira.id = :walletId and t.tipo = :type and t.data between :start and :end")
    BigDecimal sumByWalletAndTypeAndPeriod(
        @Param("walletId") Long walletId,
        @Param("type") TipoTransacao type,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end);

    void deleteByCarteiraId(Long carteiraId);
}
