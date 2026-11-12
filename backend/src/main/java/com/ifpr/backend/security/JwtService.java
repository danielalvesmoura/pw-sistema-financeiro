package com.ifpr.backend.security;

import com.ifpr.backend.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expirationMs
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET deve ser configurado no ambiente.");
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET deve possuir pelo menos 32 bytes.");
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION deve ser maior que zero.");
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(Usuario usuario) {
        Date now = new Date();

        return Jwts.builder()
            .subject(usuario.getId().toString())
            .claim("email", usuario.getEmail())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expirationMs))
            .signWith(key)
            .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("email", String.class);
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
