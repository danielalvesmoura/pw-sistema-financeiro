package com.ifpr.backend.service;

import static com.ifpr.backend.dto.AuthDtos.*;

import com.ifpr.backend.exception.BadRequestException;
import com.ifpr.backend.exception.ConflictException;
import com.ifpr.backend.exception.UnauthorizedException;
import com.ifpr.backend.model.TokenRedefinicaoSenha;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.TokenRedefinicaoSenhaRepository;
import com.ifpr.backend.repository.UsuarioRepository;
import com.ifpr.backend.security.JwtService;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final boolean exposeResetToken;

    public AuthService(
        UsuarioRepository usuarioRepository,
        TokenRedefinicaoSenhaRepository tokenRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        @Value("${app.security.expose-reset-token:false}") boolean exposeResetToken
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.exposeResetToken = exposeResetToken;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();

        usuario.setNome(request.name().trim());
        usuario.setEmail(email);
        usuario.setSenhaCriptografada(passwordEncoder.encode(request.password()));
        usuario.setMoedaPadrao("BRL");

        usuario = usuarioRepository.save(usuario);

        return new RegisterResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getCriadoEm());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    email,
                    request.password()
                )
            );
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos."));

        usuario.setUltimoAcessoEm(LocalDateTime.now());

        usuarioRepository.save(usuario);

        return new LoginResponse(jwtService.generateToken(usuario), "Bearer", jwtService.getExpirationSeconds());
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request, String ipOrigem) {
        String message = "Se este e-mail estiver cadastrado, você receberá as instruções em breve.";
        String email = normalizeEmail(request.email());

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

        if (usuario == null) {
            return new ForgotPasswordResponse(message, null);
        }

        TokenRedefinicaoSenha token = new TokenRedefinicaoSenha();

        token.setUsuario(usuario);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiraEm(LocalDateTime.now().plusHours(1));
        token.setUtilizado(false);
        token.setIpOrigem(ipOrigem);
        token.setTipoSolicitacao("RECUPERACAO_SENHA");

        tokenRepository.save(token);

        String debugToken = exposeResetToken ? token.getToken() : null;

        return new ForgotPasswordResponse(message, debugToken);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        TokenRedefinicaoSenha token = tokenRepository.findByToken(request.token())
            .orElseThrow(() -> new BadRequestException("Token inválido."));

        if (token.isUtilizado() || token.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token inválido, expirado ou já utilizado.");
        }

        Usuario usuario = token.getUsuario();

        usuario.setSenhaCriptografada(passwordEncoder.encode(request.newPassword()));
        token.setUtilizado(true);
        token.setUtilizadoEm(LocalDateTime.now());

        usuarioRepository.save(usuario);
        tokenRepository.save(token);

        return new MessageResponse("Senha redefinida com sucesso.");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
