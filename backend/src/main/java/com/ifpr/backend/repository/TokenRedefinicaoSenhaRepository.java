package com.ifpr.backend.repository;

import com.ifpr.backend.model.TokenRedefinicaoSenha;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {
    Optional<TokenRedefinicaoSenha> findByToken(String token);
}
