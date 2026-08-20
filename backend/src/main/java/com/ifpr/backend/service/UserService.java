package com.ifpr.backend.service;

import static com.ifpr.backend.dto.AuthDtos.MessageResponse;
import static com.ifpr.backend.dto.UserDtos.*;

import com.ifpr.backend.exception.BusinessException;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final CurrentUserService currentUserService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        CurrentUserService currentUserService,
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.currentUserService = currentUserService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse me() { return toResponse(currentUserService.get()); }

    @Transactional
    public UserResponse update(UpdateUserRequest request) {
        Usuario usuario = currentUserService.get();

        usuario.setNome(request.name().trim());

        if (request.defaultCurrency() != null && !request.defaultCurrency().isBlank()) {
            usuario.setMoedaPadrao(request.defaultCurrency().trim().toUpperCase());
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        Usuario usuario = currentUserService.get();

        if (!passwordEncoder.matches(request.currentPassword(), usuario.getSenhaCriptografada())) {
            throw new BusinessException("Senha atual incorreta.");
        }

        usuario.setSenhaCriptografada(passwordEncoder.encode(request.newPassword()));

        usuarioRepository.save(usuario);

        return new MessageResponse("Senha alterada com sucesso.");
    }

    private UserResponse toResponse(Usuario usuario) {
        return new UserResponse(
            usuario.getId(), 
            usuario.getNome(), 
            usuario.getEmail(), 
            usuario.getMoedaPadrao(),
            usuario.isAtivo(), 
            usuario.getUltimoAcessoEm(), 
            usuario.getCriadoEm(), 
            usuario.getAtualizadoEm()
        );
    }
}
