package com.ifpr.backend.service;

import com.ifpr.backend.model.CarteiraMembro;
import com.ifpr.backend.model.PapelCarteira;
import com.ifpr.backend.repository.CarteiraMembroRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class WalletAuthorizationService {
    private final CarteiraMembroRepository membroRepository;
    private final CurrentUserService currentUserService;

    public WalletAuthorizationService(
            CarteiraMembroRepository membroRepository,
            CurrentUserService currentUserService) {
        this.membroRepository = membroRepository;
        this.currentUserService = currentUserService;
    }

    public CarteiraMembro requireMember(Long walletId) {
        Long userId = currentUserService.get().getId();
        CarteiraMembro member = membroRepository.findByCarteiraIdAndUsuarioId(walletId, userId)
                .orElseThrow(() -> new AccessDeniedException("Você não faz parte desta carteira."));
        if (!member.isAtivo() || member.isConvitePendente()) {
            throw new AccessDeniedException("Seu acesso a esta carteira não está ativo.");
        }
        return member;
    }

    public CarteiraMembro requireOwner(Long walletId) {
        CarteiraMembro member = requireMember(walletId);
        if (member.getPapel() != PapelCarteira.OWNER) {
            throw new AccessDeniedException("Somente o dono pode realizar esta ação.");
        }
        return member;
    }

    public CarteiraMembro requireEditor(Long walletId) {
        CarteiraMembro member = requireMember(walletId);
        if (member.getPapel() == PapelCarteira.VIEWER) {
            throw new AccessDeniedException("Seu acesso é somente para visualização.");
        }
        return member;
    }
}
