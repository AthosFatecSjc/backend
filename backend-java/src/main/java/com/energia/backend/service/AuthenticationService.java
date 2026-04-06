package com.energia.backend.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.LoginRequest;
import com.energia.backend.dto.LoginResponse;
import com.energia.backend.exception.LoginAuthenticationException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthenticationService {

    private final AppUserJpaRepository userRepository;
    private final UserStatusJpaRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret:sua-chave-secreta-muito-longa-com-pelo-menos-256-bits-de-comprimento-para-hs512}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    public AuthenticationService(
            AppUserJpaRepository userRepository,
            UserStatusJpaRepository userStatusRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userStatusRepository = userStatusRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest request) {
        validarRequest(request);

        // 1. Buscar usuário pelo email
        AppUserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new LoginAuthenticationException(
                        "Invalid email or password",
                        "INVALID_CREDENTIALS",
                        HttpStatus.UNAUTHORIZED.value()
                ));

        // 2. Verificar senha com BCrypt
        if (!passwordEncoder.matches(request.getSenha(), user.getPassword())) {
            throw new LoginAuthenticationException(
                    "Invalid email or password",
                    "INVALID_CREDENTIALS",
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        // 3. Verificar status do usuário
        UserStatusEntity userStatus = userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)
                .orElseThrow(() -> new LoginAuthenticationException(
                        "User account has no status assigned",
                        "USER_NO_STATUS",
                        HttpStatus.FORBIDDEN.value()
                ));

        String statusName = userStatus.getStatus().getName();

        if ("PENDENTE".equals(statusName)) {
            throw new LoginAuthenticationException(
                    "User account is pending administrator approval",
                    "USER_PENDING_APPROVAL",
                    HttpStatus.FORBIDDEN.value()
            );
        }

        if ("REJEITADO".equals(statusName)) {
            String reason = userStatus.getRationaleForRejection() != null
                    ? userStatus.getRationaleForRejection()
                    : "No reason provided";
            throw new LoginAuthenticationException(
                    "User account has been rejected",
                    "USER_REJECTED",
                    HttpStatus.FORBIDDEN.value(),
                    reason
            );
        }

        if (!"APROVADO".equals(statusName)) {
            throw new LoginAuthenticationException(
                    "User account status is invalid: " + statusName,
                    "INVALID_USER_STATUS",
                    HttpStatus.FORBIDDEN.value()
            );
        }

        // 4. Extrair roles do usuário (simplificado: apenas "admin" ou "user")
        List<String> roles = user.getRoles() != null 
                ? user.getRoles().stream()
                    .map(role -> role.getName().toLowerCase().replaceAll("role_", ""))
                    .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        // 5. Gerar JWT com roles
        String token = generateTokenWithRoles(user.getId(), user.getEmail(), user.getName(), roles);

        // 6. Retornar resposta com sucesso
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    private void validarRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login payload is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    // ======== JWT Methods ========

    /**
     * Gera um JWT token com roles inclusos
     */
    public String generateTokenWithRoles(UUID userId, String email, String username, List<String> roles) {
        Map<String, Object> claims = Map.of(
            "userId", userId.toString(),
            "email", email,
            "username", username,
            "roles", roles
        );
        return createToken(claims, userId.toString());
    }

    /**
     * Cria o token assinado HS512
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Valida se o token é válido (não expirado e assinado corretamente)
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extrai o userId do token
     */
    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.get("userId", String.class));
    }

    /**
     * Extrai o email do token
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extrai username do token
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("username", String.class);
    }

    /**
     * Extrai os roles do token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", java.util.List.class);
    }

    /**
     * Extrai todas as claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
