package com.ifpr.backend.repository;

import com.ifpr.backend.model.Transacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {
    boolean existsByCategoriaId(Long categoriaId);
    List<Transacao> findByCarteiraId(Long carteiraId);
    void deleteByCarteiraId(Long carteiraId);
}
