package com.energia.backend.service;

import com.energia.backend.dto.LoginRequest;
import com.energia.backend.dto.LoginResponse;
import com.energia.backend.exception.LoginAuthenticationException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

    private final AppUserJpaRepository userRepository;
    private final UserStatusJpaRepository userStatusRepository;

    @Value("${jwt.secret:sua-chave-secreta-muito-longa-com-pelo-menos-256-bits-de-comprimento}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    public AuthenticationService(
            AppUserJpaRepository userRepository,
            UserStatusJpaRepository userStatusRepository
    ) {
        this.userRepository = userRepository;
        this.userStatusRepository = userStatusRepository;
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

        // 2. Verificar senha
        if (!verificarSenha(request.getSenha(), user.getPassword())) {
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

        if (!"ATIVO".equals(statusName)) {
            throw new LoginAuthenticationException(
                    "User account status is invalid: " + statusName,
                    "INVALID_USER_STATUS",
                    HttpStatus.FORBIDDEN.value()
            );
        }

        // 4. Gerar JWT
        String token = gerarJWT(user.getId(), user.getEmail(), user.getName());

        // 5. Retornar resposta com sucesso
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

    private boolean verificarSenha(String senhaProvidenciada, String senhaHashArmazenada) {
        try {
            // Formato armazenado: PBKDF2$<iterations>$<salt_base64>$<hash_base64>
            String[] partes = senhaHashArmazenada.split("\\$");
            if (partes.length != 4 || !partes[0].equals("PBKDF2")) {
                return false;
            }

            int iterations = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            String hashArmazenado = partes[3];

            // Recompor o hash com a senha fornecida
            PBEKeySpec spec = new PBEKeySpec(
                    senhaProvidenciada.toCharArray(),
                    salt,
                    iterations,
                    KEY_LENGTH
            );

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hashComputed = keyFactory.generateSecret(spec).getEncoded();
            String hashComputedBase64 = Base64.getEncoder().encodeToString(hashComputed);

            return hashArmazenado.equals(hashComputedBase64);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Falha ao verificar senha", e);
        }
    }

    private String gerarJWT(java.util.UUID userId, String email, String nome) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("nome", nome)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }
}
