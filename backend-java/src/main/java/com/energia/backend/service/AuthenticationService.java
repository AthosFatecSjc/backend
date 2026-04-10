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
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthenticationService {

    private final AppUserJpaRepository userRepository;
    private final UserStatusService userStatusService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret:sua-chave-secreta-muito-longa-com-pelo-menos-256-bits-de-comprimento-para-hs512}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    public AuthenticationService(
            AppUserJpaRepository userRepository,
            UserStatusService userStatusService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userStatusService = userStatusService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest request) {
        validarRequest(request);

        AppUserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new LoginAuthenticationException(
                        "Invalid email or password",
                        "INVALID_CREDENTIALS",
                        HttpStatus.UNAUTHORIZED.value()
                ));

        if (!passwordEncoder.matches(request.getSenha(), user.getPassword())) {
            throw new LoginAuthenticationException(
                    "Invalid email or password",
                    "INVALID_CREDENTIALS",
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        UserStatusEntity userStatus = userStatusService.resolveCurrentStatusEntry(user)
                .orElseThrow(() -> new LoginAuthenticationException(
                        "User account has no status assigned",
                        "USER_NO_STATUS",
                        HttpStatus.FORBIDDEN.value()
                ));

        String statusName = userStatus.getStatus() != null ? userStatus.getStatus().getName() : null;
        StatusUsuario status = userStatusService.toOfficialStatus(statusName);

        if (status == StatusUsuario.PENDENTE) {
            throw new LoginAuthenticationException(
                    "User account is pending administrator approval",
                    "USER_PENDING_APPROVAL",
                    HttpStatus.FORBIDDEN.value()
            );
        }

        if (status == StatusUsuario.REJEITADO) {
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

        if (status != StatusUsuario.ATIVO) {
            throw new LoginAuthenticationException(
                    "User account status is invalid: " + statusName,
                    "INVALID_USER_STATUS",
                    HttpStatus.FORBIDDEN.value()
            );
        }

        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                    .map(role -> role.getName().toLowerCase().replaceAll("role_", ""))
                    .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        String token = generateTokenWithRoles(user.getId(), user.getEmail(), user.getName(), roles);
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

    public String generateTokenWithRoles(UUID userId, String email, String username, List<String> roles) {
        Map<String, Object> claims = Map.of(
            "userId", userId.toString(),
            "email", email,
            "username", username,
            "roles", roles
        );
        return createToken(claims, userId.toString());
    }

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

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.get("userId", String.class));
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", java.util.List.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
