package com.ifpr.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifpr.backend.exception.ApiError;
import com.ifpr.backend.security.JwtAuthenticationFilter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;
    private final List<String> allowedOrigins;

    public SecurityConfig(
        JwtAuthenticationFilter jwtFilter,
        ObjectMapper objectMapper,
        @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
        this.allowedOrigins = parseAllowedOrigins(allowedOrigins);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // A API usa JWT no header Authorization e nao cookies de sessao.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data:; " +
                    "font-src 'self' data:; " +
                    "connect-src 'self'; " +
                    "object-src 'none'; " +
                    "base-uri 'self'; " +
                    "frame-ancestors 'none'; " +
                    "form-action 'self'"
                ))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                ))
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .preload(true)
                    .maxAgeInSeconds(31536000)
                )
                .addHeaderWriter(new StaticHeadersWriter(
                    "Permissions-Policy",
                    "camera=(), microphone=(), geolocation=()"
                ))
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )

            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    objectMapper.writeValue(response.getWriter(), new ApiError(
                        LocalDateTime.now(),
                        401,
                        "Unauthorized",
                        "Token ausente ou inválido."
                    ));
                })
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    objectMapper.writeValue(response.getWriter(), new ApiError(
                        LocalDateTime.now(),
                        403,
                        "Forbidden",
                        "Acesso negado."
                    ));
                }))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        config.setExposedHeaders(List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_DISPOSITION,
            HttpHeaders.RETRY_AFTER
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private static List<String> parseAllowedOrigins(String rawOrigins) {
        List<String> origins = Arrays.stream(rawOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toList();

        if (origins.isEmpty()) {
            throw new IllegalStateException("Configure ao menos uma origem em APP_CORS_ALLOWED_ORIGINS.");
        }

        if (origins.contains("*")) {
            throw new IllegalStateException(
                "APP_CORS_ALLOWED_ORIGINS nao pode usar '*' com credenciais habilitadas."
            );
        }

        return origins;
    }
}
