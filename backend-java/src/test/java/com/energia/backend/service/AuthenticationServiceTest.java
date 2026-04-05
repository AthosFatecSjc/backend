package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.energia.backend.dto.LoginRequest;
import com.energia.backend.dto.LoginResponse;
import com.energia.backend.exception.LoginAuthenticationException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;

@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    private AuthenticationService authenticationService;

    @Mock
    private AppUserJpaRepository userRepository;

    @Mock
    private UserStatusJpaRepository userStatusRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UUID userId;
    private String email;
    private String nome;
    private String password;
    private String passwordHashBcrypt;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(
            userRepository,
            userStatusRepository,
            passwordEncoder
        );

        userId = UUID.randomUUID();
        email = "user@example.com";
        nome = "Test User";
        password = "senha123456";

        // BCrypt hash for "senha123456" (generated with strength=10)
        passwordHashBcrypt = "$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy990RK";
    }

    @Test
    @DisplayName("Scenario 1: ATIVO user can login successfully")
    void testLogin_ActiveUserSucceeds() {
        // Arrange
        RoleEntity userRole = RoleEntity.builder().id(UUID.randomUUID()).name("user").build();
        AppUserEntity user = criarAppUserEntity(userId, email, nome, passwordHashBcrypt, List.of(userRole));
        UserStatusEntity userStatus = criarUserStatus(user, "ATIVO");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)).thenReturn(Optional.of(userStatus));
        when(passwordEncoder.matches(password, passwordHashBcrypt)).thenReturn(true);

        LoginRequest request = new LoginRequest(email, password);

        // Act
        LoginResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals(nome, response.getNome());
        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());

        // Valida que o token contém as informações corretas
        String token = response.getAccessToken();
        assertTrue(authenticationService.isTokenValid(token));
        assertEquals(userId, authenticationService.extractUserId(token));
        assertEquals(email, authenticationService.extractEmail(token));

        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
        verify(userStatusRepository, times(1)).findFirstByUserOrderByAssignedAtDesc(user);
        verify(passwordEncoder, times(1)).matches(password, passwordHashBcrypt);
    }

    @Test
    @DisplayName("Scenario 2: PENDENTE user cannot login")
    void testLogin_PendingUserFails() {
        // Arrange
        RoleEntity userRole = RoleEntity.builder().id(UUID.randomUUID()).name("user").build();
        AppUserEntity user = criarAppUserEntity(userId, email, nome, passwordHashBcrypt, List.of(userRole));
        UserStatusEntity userStatus = criarUserStatus(user, "PENDENTE");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)).thenReturn(Optional.of(userStatus));
        when(passwordEncoder.matches(password, passwordHashBcrypt)).thenReturn(true);

        LoginRequest request = new LoginRequest(email, password);

        // Act & Assert
        LoginAuthenticationException exception = assertThrows(
            LoginAuthenticationException.class,
            () -> authenticationService.authenticate(request),
            "Should throw LoginAuthenticationException for PENDENTE user"
        );

        assertEquals("USER_PENDING_APPROVAL", exception.getErrorCode());
        assertEquals(403, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("pending administrator approval"));
    }

    @Test
    @DisplayName("Scenario 3: REJEITADO user cannot login")
    void testLogin_RejectedUserFails() {
        // Arrange
        RoleEntity userRole = RoleEntity.builder().id(UUID.randomUUID()).name("user").build();
        AppUserEntity user = criarAppUserEntity(userId, email, nome, passwordHashBcrypt, List.of(userRole));
        UserStatusEntity userStatus = criarUserStatus(user, "REJEITADO");
        userStatus.setRationaleForRejection("Failed security check");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)).thenReturn(Optional.of(userStatus));
        when(passwordEncoder.matches(password, passwordHashBcrypt)).thenReturn(true);

        LoginRequest request = new LoginRequest(email, password);

        // Act & Assert
        LoginAuthenticationException exception = assertThrows(
            LoginAuthenticationException.class,
            () -> authenticationService.authenticate(request),
            "Should throw LoginAuthenticationException for REJEITADO user"
        );

        assertEquals("USER_REJECTED", exception.getErrorCode());
        assertEquals(403, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("rejected"));
        assertEquals("Failed security check", exception.getReason());
    }

    @Test
    @DisplayName("Scenario 4: Invalid credentials are rejected")
    void testLogin_InvalidCredentialsFail() {
        // Arrange
        RoleEntity userRole = RoleEntity.builder().id(UUID.randomUUID()).name("user").build();
        AppUserEntity user = criarAppUserEntity(userId, email, nome, passwordHashBcrypt, List.of(userRole));

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", passwordHashBcrypt)).thenReturn(false);

        LoginRequest request = new LoginRequest(email, "wrongPassword");

        // Act & Assert
        LoginAuthenticationException exception = assertThrows(
            LoginAuthenticationException.class,
            () -> authenticationService.authenticate(request),
            "Should throw LoginAuthenticationException for invalid password"
        );

        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        assertEquals(401, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    @Test
    @DisplayName("Scenario 5: Non-existent user cannot login")
    void testLogin_NonExistentUserFails() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nonexistent@example.com", password);

        // Act & Assert
        LoginAuthenticationException exception = assertThrows(
            LoginAuthenticationException.class,
            () -> authenticationService.authenticate(request),
            "Should throw LoginAuthenticationException for non-existent user"
        );

        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        assertEquals(401, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Should reject null email")
    void testLogin_NullEmailFails() {
        // Arrange
        LoginRequest request = new LoginRequest(null, password);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> authenticationService.authenticate(request),
            "Should throw IllegalArgumentException for null email"
        );
    }

    @Test
    @DisplayName("Should reject null password")
    void testLogin_NullPasswordFails() {
        // Arrange
        LoginRequest request = new LoginRequest(email, null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> authenticationService.authenticate(request),
            "Should throw IllegalArgumentException for null password"
        );
    }

    // Helper methods
    private AppUserEntity criarAppUserEntity(UUID id, String email, String nome, String password, List<RoleEntity> roles) {
        AppUserEntity user = new AppUserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setName(nome);
        user.setPassword(password);
        user.setRoles(roles);
        return user;
    }

    private UserStatusEntity criarUserStatus(AppUserEntity user, String statusName) {
        StatusEntity status = new StatusEntity();
        status.setId(UUID.randomUUID());
        status.setName(statusName);

        UserStatusEntity userStatus = new UserStatusEntity();
        userStatus.setId(UUID.randomUUID());
        userStatus.setUser(user);
        userStatus.setStatus(status);
        userStatus.setAssignedAt(LocalDateTime.now());
        return userStatus;
    }
}
