package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

class UsuarioCadastroServiceAprovacaoRejeicaoTest {

    private UsuarioCadastroRepository usuarioCadastroRepository;
    private AppUserJpaRepository appUserRepository;
    private TermsService termsService;
    private StatusJpaRepository statusRepository;
    private UserStatusJpaRepository userStatusRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioCadastroService service;

    private UUID usuarioId;
    private UUID adminId;
    private AppUserEntity usuario;
    private AppUserEntity admin;

    @BeforeEach
    void setup() {
        usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
        appUserRepository = mock(AppUserJpaRepository.class);
        termsService = mock(TermsService.class);
        statusRepository = mock(StatusJpaRepository.class);
        userStatusRepository = mock(UserStatusJpaRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UsuarioCadastroService(
                usuarioCadastroRepository,
                appUserRepository,
                termsService,
                statusRepository,
                userStatusRepository,
                passwordEncoder
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
    void deveAprovarUsuarioComRegistroDeStatus() {
        mockStatusAtual("PENDENTE");
        when(statusRepository.findByNameIgnoreCase("APROVADO"))
                .thenReturn(Optional.of(StatusEntity.builder().id(UUID.randomUUID()).name("APROVADO").build()));

        service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.APROVADO, null);

        ArgumentCaptor<UserStatusEntity> captor = ArgumentCaptor.forClass(UserStatusEntity.class);
        verify(userStatusRepository).save(captor.capture());
        UserStatusEntity status = captor.getValue();

        assertEquals(usuario, status.getUser());
        assertEquals(admin, status.getAssignedBy());
        assertEquals("APROVADO", status.getStatus().getName());
        assertNull(status.getRationaleForRejection());
    }

    @Test
    void deveRejeitarUsuarioComMotivo() {
        mockStatusAtual("PENDENTE");
        when(statusRepository.findByNameIgnoreCase("REJEITADO"))
                .thenReturn(Optional.of(StatusEntity.builder().id(UUID.randomUUID()).name("REJEITADO").build()));

        service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, "Dados inconsistentes");

        ArgumentCaptor<UserStatusEntity> captor = ArgumentCaptor.forClass(UserStatusEntity.class);
        verify(userStatusRepository).save(captor.capture());
        UserStatusEntity status = captor.getValue();

        assertEquals("REJEITADO", status.getStatus().getName());
        assertEquals("Dados inconsistentes", status.getRationaleForRejection());
    }

    @Test
    void rejeicaoSemMotivoDeveLancarExcecao() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, " ")
        );

        assertEquals("Motivo da rejeicao e obrigatorio.", ex.getMessage());
    }

    @Test
    void naoPermiteAlterarStatusSeNaoForAdmin() {
        admin.setRoles(List.of());
        mockStatusAtual("PENDENTE");

        PermissaoNegadaException ex = assertThrows(
                PermissaoNegadaException.class,
                () -> service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.APROVADO, null)
        );

        assertEquals("Apenas administradores podem alterar o status de usuarios.", ex.getMessage());
    }

    @Test
    void naoPermiteAlterarStatusSeNaoEstiverPendente() {
        mockStatusAtual("APROVADO");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, "motivo")
        );

        assertEquals("So e permitido aprovar ou rejeitar usuarios com status PENDENTE.", ex.getMessage());
    }

    @Test
    void naoPermiteAlterarParaStatusDiferenteDeAprovadoOuRejeitado() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.PENDENTE, null)
        );

        assertEquals("Status deve ser APROVADO ou REJEITADO.", ex.getMessage());
    }

    private void mockStatusAtual(String nomeStatus) {
        UserStatusEntity statusAtual = UserStatusEntity.builder()
                .user(usuario)
                .status(StatusEntity.builder().id(UUID.randomUUID()).name(nomeStatus).build())
                .assignedBy(admin)
                .assignedAt(LocalDateTime.now().minusMinutes(10))
                .build();

        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(usuario))
                .thenReturn(Optional.of(statusAtual));
        when(userStatusRepository.save(any(UserStatusEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
