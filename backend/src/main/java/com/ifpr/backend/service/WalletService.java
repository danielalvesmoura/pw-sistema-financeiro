package com.ifpr.backend.service;

import static com.ifpr.backend.dto.WalletDtos.*;

import com.ifpr.backend.exception.BusinessException;
import com.ifpr.backend.exception.ConflictException;
import com.ifpr.backend.exception.ResourceNotFoundException;
import com.ifpr.backend.model.*;
import com.ifpr.backend.repository.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
    private final CarteiraRepository carteiraRepository;
    private final CarteiraMembroRepository membroRepository;
    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;
    private final WalletAuthorizationService auth;

    public WalletService(
        CarteiraRepository carteiraRepository,
        CarteiraMembroRepository membroRepository,
        TransacaoRepository transacaoRepository,
        CategoriaRepository categoriaRepository,
        UsuarioRepository usuarioRepository,
        CurrentUserService currentUserService,
        WalletAuthorizationService auth
    ) {
        this.carteiraRepository = carteiraRepository;
        this.membroRepository = membroRepository;
        this.transacaoRepository = transacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.currentUserService = currentUserService;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> list() {
        Usuario user = currentUserService.get();

        return membroRepository.findByUsuarioIdOrderByEntradoEmAsc(user.getId()).stream()
            .filter(CarteiraMembro::isAtivo)
            .map(member -> toResponse(member.getCarteira(), member.getPapel()))
            .toList();
    }

    @Transactional
    public WalletResponse create(WalletRequest request) {
        Usuario user = currentUserService.get();
        Carteira wallet = new Carteira();

        wallet.setDono(user);

        apply(wallet, request, true);

        wallet = carteiraRepository.save(wallet);

        CarteiraMembro member = new CarteiraMembro();

        member.setCarteira(wallet);
        member.setUsuario(user);
        member.setPapel(PapelCarteira.OWNER);
        member.setConvitePendente(false);
        member.setAtivo(true);

        member = membroRepository.save(member);

        return toResponse(wallet, member.getPapel());
    }

    @Transactional(readOnly = true)
    public WalletResponse get(Long id) {
        CarteiraMembro member = auth.requireMember(id);

        return toResponse(member.getCarteira(), member.getPapel());
    }

    @Transactional
    public WalletResponse update(Long id, WalletRequest request) {
        CarteiraMembro member = auth.requireOwner(id);
        Carteira wallet = member.getCarteira();

        apply(wallet, request, false);

        return toResponse(carteiraRepository.save(wallet), member.getPapel());
    }

    @Transactional
    public void delete(Long id) {
        auth.requireOwner(id);

        Carteira wallet = carteiraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));

        transacaoRepository.deleteByCarteiraId(id);
        categoriaRepository.deleteByCarteiraId(id);
        membroRepository.deleteByCarteiraId(id);
        carteiraRepository.delete(wallet);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(Long walletId) {
        auth.requireMember(walletId);

        return membroRepository.findByCarteiraIdOrderByEntradoEmAsc(walletId).stream()
            .map(this::toMemberResponse)
            .toList();
    }

    @Transactional
    public MemberResponse addMember(Long walletId, AddMemberRequest request) {
        auth.requireOwner(walletId);

        if (request.role() == PapelCarteira.OWNER) {
            throw new BusinessException("Novo membro deve ser EDITOR ou VIEWER.");
        }

        Usuario user = usuarioRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (membroRepository.existsByCarteiraIdAndUsuarioId(walletId, user.getId())) {
            throw new ConflictException("Usuário já é membro desta carteira.");
        }

        Carteira wallet = carteiraRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));

        CarteiraMembro member = new CarteiraMembro();

        member.setCarteira(wallet);
        member.setUsuario(user);
        member.setPapel(request.role());
        member.setConvitePendente(false);
        member.setAtivo(true);

        return toMemberResponse(membroRepository.save(member));
    }

    @Transactional
    public MemberResponse updateMember(Long walletId, Long userId, UpdateMemberRoleRequest request) {
        auth.requireOwner(walletId);

        CarteiraMembro member = membroRepository.findByCarteiraIdAndUsuarioId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado."));

        if (member.getPapel() == PapelCarteira.OWNER || request.role() == PapelCarteira.OWNER) {
            throw new BusinessException("O papel do dono da carteira não pode ser alterado.");
        }

        member.setPapel(request.role());

        return toMemberResponse(membroRepository.save(member));
    }

    @Transactional
    public void removeMember(Long walletId, Long userId) {
        auth.requireOwner(walletId);

        CarteiraMembro member = membroRepository.findByCarteiraIdAndUsuarioId(walletId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado."));

        if (member.getPapel() == PapelCarteira.OWNER) {
            throw new BusinessException("O dono não pode ser removido da carteira.");
        }

        membroRepository.delete(member);
    }

    private void apply(Carteira wallet, WalletRequest request, boolean creating) {
        wallet.setNome(request.name().trim());
        wallet.setDescricao(blankToNull(request.description()));

        if (request.currency() != null && !request.currency().isBlank()) {
            wallet.setMoeda(request.currency().trim().toUpperCase());
        } else if (creating) {
            wallet.setMoeda("BRL");
        }

        if (request.initialBalance() != null) {
            wallet.setSaldoInicial(request.initialBalance());
        } else if (creating) {
            wallet.setSaldoInicial(BigDecimal.ZERO);
        }

        if (request.archived() != null) {
            wallet.setArquivada(request.archived());
        } else if (creating) {
            wallet.setArquivada(false);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private WalletResponse toResponse(Carteira wallet, PapelCarteira role) {
        return new WalletResponse(
            wallet.getId(),
            wallet.getNome(),
            wallet.getDescricao(),
            wallet.getMoeda(),
            wallet.getSaldoInicial(),
            wallet.isArquivada(),
            wallet.getDono().getId(),
            wallet.getDono().getNome(),
            role,
            wallet.getCriadoEm(),
            wallet.getAtualizadoEm()
        );
    }

    private MemberResponse toMemberResponse(CarteiraMembro member) {
        Usuario user = member.getUsuario();
        
        return new MemberResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                member.getPapel(),
                member.getEntradoEm(),
                member.isConvitePendente(),
                member.getConviteExpiraEm(),
                member.isAtivo());
    }
}
