package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.AuthDtos.*;

import com.ifpr.backend.security.AuthRateLimitService;
import com.ifpr.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Autenticação")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;
    private final AuthRateLimitService rateLimitService;

    public AuthController(
        AuthService service,
        AuthRateLimitService rateLimitService
    ) {
        this.service = service;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Autentica e retorna o token JWT")
    @PostMapping("/login")
    public LoginResponse login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String ipAddress = httpRequest.getRemoteAddr();

        rateLimitService.checkLogin(ipAddress, request.email());

        LoginResponse response = service.login(request);

        rateLimitService.loginSucceeded(ipAddress, request.email());

        return response;
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgot(
        @Valid @RequestBody ForgotPasswordRequest request,
        HttpServletRequest httpRequest
    ) {
        String ipAddress = httpRequest.getRemoteAddr();

        rateLimitService.checkPasswordRecovery(ipAddress, request.email());

        return service.forgotPassword(request, ipAddress);
    }

    @PostMapping("/reset-password")
    public MessageResponse reset(@Valid @RequestBody ResetPasswordRequest request) {
        return service.resetPassword(request);
    }
}
