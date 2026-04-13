package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.JpaUsuarioCadastroRepository;
import com.energia.backend.repository.RoleJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

class UsuarioCadastroServiceAprovacaoRejeicaoTest {

    private UsuarioCadastroRepository usuarioCadastroRepository;
    private AppUserJpaRepository appUserRepository;
    private TermsUserService termsUserService;
    private StatusJpaRepository statusRepository;
    private UserStatusJpaRepository userStatusRepository;
    private TermsService termsService;
    private UserStatusService userStatusService;
    private PasswordEncoder passwordEncoder;
    private UsuarioCadastroService service;
    private JpaUsuarioCadastroRepository jpaUsuarioCadastroRepository;
    private RoleJpaRepository roleJpaRepository;

    private UUID usuarioId;
    private UUID adminId;
    private AppUserEntity usuario;
    private AppUserEntity admin;

    @BeforeEach
    void setup() {
        usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
        appUserRepository = mock(AppUserJpaRepository.class);
        termsUserService = mock(TermsUserService.class);
        statusRepository = mock(StatusJpaRepository.class);
        userStatusRepository = mock(UserStatusJpaRepository.class);
        termsService = mock(TermsService.class);
        userStatusService = mock(UserStatusService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jpaUsuarioCadastroRepository = mock(JpaUsuarioCadastroRepository.class);
        roleJpaRepository = mock(RoleJpaRepository.class);
        service = new UsuarioCadastroService(
                usuarioCadastroRepository,
                appUserRepository,
                termsUserService,
                statusRepository,
                userStatusRepository,
                termsService,
                userStatusService,
                passwordEncoder,
                jpaUsuarioCadastroRepository,
                roleJpaRepository
        );

        usuarioId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        usuario = AppUserEntity.builder()
                .id(usuarioId)
                .name("User")
                .email("user@x.com")
                .build();

        admin = AppUserEntity.builder()
                .id(adminId)
                .name("Admin")
                .email("admin@x.com")
                .roles(List.of(RoleEntity.builder().id(UUID.randomUUID()).name("ADMIN").build()))
                .build();

        when(appUserRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
    }

    @Test
    void deveAtivarUsuarioComDelegacaoParaUserStatusService() {
        service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.ATIVO, null);

        verify(userStatusService).transitionFromPending(usuario, admin, StatusUsuario.ATIVO, null);
    }

    @Test
    void deveRejeitarUsuarioComMotivoDelegandoParaUserStatusService() {
        service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, "Dados inconsistentes");

        verify(userStatusService).transitionFromPending(usuario, admin, StatusUsuario.REJEITADO, "Dados inconsistentes");
    }

    @Test
    void rejeicaoSemMotivoDevePropagarExcecaoDoUserStatusService() {
        doThrow(new IllegalArgumentException("Motivo da rejeicao e obrigatorio."))
                .when(userStatusService)
                .transitionFromPending(any(AppUserEntity.class), any(AppUserEntity.class), eq(StatusUsuario.REJEITADO), eq(" "));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, " ")
        );

        assertEquals("Motivo da rejeicao e obrigatorio.", ex.getMessage());
    }

    @Test
    void naoPermiteAlterarStatusSeNaoForAdmin() {
        admin.setRoles(List.of());

        PermissaoNegadaException ex = assertThrows(
                PermissaoNegadaException.class,
                () -> service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.ATIVO, null)
        );

        assertEquals("Apenas administradores podem alterar o status de usuarios.", ex.getMessage());
    }
}
