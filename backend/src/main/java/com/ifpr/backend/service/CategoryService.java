package com.ifpr.backend.service;

import static com.ifpr.backend.dto.CategoryDtos.*;

import com.ifpr.backend.exception.BusinessException;
import com.ifpr.backend.exception.ResourceNotFoundException;
import com.ifpr.backend.model.Carteira;
import com.ifpr.backend.model.Categoria;
import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.CarteiraRepository;
import com.ifpr.backend.repository.CategoriaRepository;
import com.ifpr.backend.repository.TransacaoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;
    private final CarteiraRepository carteiraRepository;
    private final CurrentUserService currentUserService;
    private final WalletAuthorizationService auth;

    public CategoryService(
        CategoriaRepository categoriaRepository,
        TransacaoRepository transacaoRepository,
        CarteiraRepository carteiraRepository,
        CurrentUserService currentUserService,
        WalletAuthorizationService auth
    ) {
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
        this.carteiraRepository = carteiraRepository;
        this.currentUserService = currentUserService;
        this.auth = auth;
    }

    @Transactional
    public List<CategoryResponse> list(Long walletId, TipoTransacao type) {
        auth.requireMember(walletId);

        Usuario currentUser = currentUserService.get();

        migrateLegacyCategoriesIfNeeded(walletId, currentUser);
        
        List<Categoria> categorias = type == null
            ? categoriaRepository.findByCarteiraIdAndUsuarioIdOrderByOrdemExibicaoAscNomeAsc(walletId, currentUser.getId())
            : categoriaRepository.findByCarteiraIdAndUsuarioIdAndTipoOrderByOrdemExibicaoAscNomeAsc(walletId, currentUser.getId(), type);
            
        return categorias.stream().map(this::toResponse).toList();
    }

    @Transactional
    public CategoryResponse create(Long walletId, CategoryRequest request) {
        auth.requireEditor(walletId);
        Carteira wallet = getWallet(walletId);
        Categoria categoria = new Categoria();
        categoria.setCarteira(wallet);
        categoria.setUsuario(currentUserService.get());
        apply(categoria, request, true);
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoryResponse update(Long walletId, Long id, CategoryRequest request) {
        auth.requireEditor(walletId);
        Categoria categoria = findOwnedInWallet(walletId, id);
        apply(categoria, request, false);
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void delete(Long walletId, Long id) {
        auth.requireEditor(walletId);
        Categoria categoria = findOwnedInWallet(walletId, id);
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new BusinessException("Não é possível excluir uma categoria que possui transações vinculadas.");
        }
        categoriaRepository.delete(categoria);
    }

    public Categoria findOwnedInWallet(Long walletId, Long id) {
        Long currentUserId = currentUserService.get().getId();
        return categoriaRepository.findByIdAndCarteiraIdAndUsuarioId(id, walletId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    }

    private void migrateLegacyCategoriesIfNeeded(Long walletId, Usuario currentUser) {
        if (!categoriaRepository.findByCarteiraIdAndUsuarioIdOrderByOrdemExibicaoAscNomeAsc(
                walletId, currentUser.getId()).isEmpty()) return;

        Carteira wallet = getWallet(walletId);
        List<Categoria> legacy = categoriaRepository
                .findByUsuarioIdAndCarteiraIsNullOrderByOrdemExibicaoAscNomeAsc(currentUser.getId());
        if (legacy.isEmpty()) return;

        List<Categoria> copies = legacy.stream().map(old -> {
            Categoria copy = new Categoria();
            copy.setCarteira(wallet);
            copy.setUsuario(currentUser);
            copy.setNome(old.getNome());
            copy.setTipo(old.getTipo());
            copy.setIcone(old.getIcone());
            copy.setDescricao(old.getDescricao());
            copy.setOrdemExibicao(old.getOrdemExibicao());
            copy.setAtivo(old.isAtivo());
            return copy;
        }).toList();
        categoriaRepository.saveAll(copies);
    }

    private Carteira getWallet(Long walletId) {
        return carteiraRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
    }

    private void apply(Categoria categoria, CategoryRequest request, boolean creating) {
        categoria.setNome(
            request.name().trim()
        );

        categoria.setTipo(
            request.type()
        );

        categoria.setIcone(
            blankToNull(request.icon())
        );

        categoria.setDescricao(
            blankToNull(request.description())
        );
        
        if (request.displayOrder() != null) categoria.setOrdemExibicao(request.displayOrder());
        else if (creating) categoria.setOrdemExibicao(0);
        if (request.active() != null) categoria.setAtivo(request.active());
        else if (creating) categoria.setAtivo(true);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CategoryResponse toResponse(Categoria c) {
        return new CategoryResponse(
            c.getId(), 
            c.getCarteira() == null ? null : c.getCarteira().getId(),
            c.getNome(), 
            c.getTipo(), 
            c.getIcone(), 
            c.getDescricao(), 
            c.getOrdemExibicao(), 
            c.isAtivo()
        );
    }
}
