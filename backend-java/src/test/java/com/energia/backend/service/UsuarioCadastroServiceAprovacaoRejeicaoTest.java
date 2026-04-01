package com.energia.backend.service;

import com.energia.backend.model.*;
import com.energia.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioCadastroServiceAprovacaoRejeicaoTest {
    private UsuarioCadastroService service;
    private AppUserJpaRepository appUserRepo;
    private StatusJpaRepository statusRepo;
    private UserStatusJpaRepository userStatusRepo;
    private UsuarioCadastroRepository usuarioCadastroRepo;

    private UUID usuarioId;
    private UUID adminId;
    private AppUserEntity usuario;
    private AppUserEntity admin;
    private RoleEntity adminRole;
    private StatusEntity statusAprovado;
    private StatusEntity statusRejeitado;
    private List<UserStatusEntity> statusEntities;

    @BeforeEach
    void setup() {
        usuarioId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        adminRole = RoleEntity.builder().id(UUID.randomUUID()).name("ADMIN").build();
        usuario = AppUserEntity.builder().id(usuarioId).name("User").email("user@x.com").build();
        admin = AppUserEntity.builder().id(adminId).name("Admin").email("admin@x.com").roles(List.of(adminRole)).build();
        statusAprovado = StatusEntity.builder().id(UUID.randomUUID()).name("APROVADO").build();
        statusRejeitado = StatusEntity.builder().id(UUID.randomUUID()).name("REJEITADO").build();
        statusEntities = new ArrayList<>();

        appUserRepo = new AppUserJpaRepository() {
            @Override public boolean existsByEmailIgnoreCase(String email) { return false; }
            @Override public Optional<AppUserEntity> findByEmailIgnoreCase(String email) { return Optional.empty(); }
            @Override public List<AppUserEntity> findAll() { return List.of(usuario, admin); }
            @Override public List<AppUserEntity> findAllById(Iterable<UUID> uuids) { return List.of(usuario, admin); }
            @Override public <S extends AppUserEntity> S save(S entity) { return entity; }
            @Override public Optional<AppUserEntity> findById(UUID uuid) { return uuid.equals(usuarioId) ? Optional.of(usuario) : uuid.equals(adminId) ? Optional.of(admin) : Optional.empty(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(AppUserEntity entity) {}
            @Override public void deleteAll(Iterable<? extends AppUserEntity> entities) {}
            @Override public void deleteAll() {}
            @Override public long count() { return 2; }
            @Override public <S extends AppUserEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return uuid.equals(usuarioId) || uuid.equals(adminId); }
        };
        statusRepo = new StatusJpaRepository() {
            @Override public Optional<StatusEntity> findByNameIgnoreCase(String name) {
                if ("APROVADO".equalsIgnoreCase(name)) return Optional.of(statusAprovado);
                if ("REJEITADO".equalsIgnoreCase(name)) return Optional.of(statusRejeitado);
                return Optional.empty();
            }
            @Override public List<StatusEntity> findAll() { return List.of(statusAprovado, statusRejeitado); }
            @Override public List<StatusEntity> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public <S extends StatusEntity> S save(S entity) { return entity; }
            @Override public Optional<StatusEntity> findById(UUID uuid) { return Optional.empty(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(StatusEntity entity) {}
            @Override public void deleteAll(Iterable<? extends StatusEntity> entities) {}
            @Override public void deleteAll() {}
            @Override public long count() { return 2; }
            @Override public <S extends StatusEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return true; }
        };
        userStatusRepo = new UserStatusJpaRepository() {
            @Override public Optional<UserStatusEntity> findFirstByUserOrderByAssignedAtDesc(AppUserEntity user) { return Optional.empty(); }
            @Override public Optional<UserStatusEntity> findFirstByUserOrderByAssignedAtAsc(AppUserEntity user) { return Optional.empty(); }
            @Override public List<UserStatusEntity> findAll() { return statusEntities; }
            @Override public List<UserStatusEntity> findAllById(Iterable<UUID> uuids) { return null; }
            @Override public <S extends UserStatusEntity> S save(S entity) { statusEntities.add(entity); return entity; }
            @Override public Optional<UserStatusEntity> findById(UUID uuid) { return Optional.empty(); }
            @Override public void deleteById(UUID uuid) {}
            @Override public void delete(UserStatusEntity entity) {}
            @Override public void deleteAll(Iterable<? extends UserStatusEntity> entities) {}
            @Override public void deleteAll() {}
            @Override public long count() { return statusEntities.size(); }
            @Override public <S extends UserStatusEntity> List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public boolean existsById(UUID uuid) { return false; }
        };
        usuarioCadastroRepo = new UsuarioCadastroRepository() {
            @Override public boolean existsByEmail(String email) { return false; }
            @Override public com.energia.backend.model.Usuario save(com.energia.backend.model.Usuario usuario) { return usuario; }
        };
        service = new UsuarioCadastroService(usuarioCadastroRepo, appUserRepo, statusRepo, userStatusRepo);
    }

    @Test
    void deveAprovarUsuarioComRegistroDeStatus() {
        // Usuário começa como pendente
        StatusEntity statusPendente = StatusEntity.builder().id(UUID.randomUUID()).name("PENDENTE").build();
        UserStatusEntity pendente = UserStatusEntity.builder().user(usuario).status(statusPendente).assignedBy(admin).assignedAt(LocalDateTime.now().minusMinutes(10)).build();
        statusEntities.add(pendente);
        service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.APROVADO, null);
        assertEquals(2, statusEntities.size());
        UserStatusEntity status = statusEntities.get(1);
        assertEquals(usuario, status.getUser());
        assertEquals(admin, status.getAssignedBy());
        assertEquals("APROVADO", status.getStatus().getName());
        assertNotNull(status.getAssignedAt());
        assertNull(status.getRationaleForRejection());
    }

    @Test
    void deveRejeitarUsuarioComMotivo() {
        StatusEntity statusPendente = StatusEntity.builder().id(UUID.randomUUID()).name("PENDENTE").build();
        UserStatusEntity pendente = UserStatusEntity.builder().user(usuario).status(statusPendente).assignedBy(admin).assignedAt(LocalDateTime.now().minusMinutes(10)).build();
        statusEntities.add(pendente);
        String motivo = "Dados inconsistentes";
        service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, motivo);
        assertEquals(2, statusEntities.size());
        UserStatusEntity status = statusEntities.get(1);
        assertEquals(usuario, status.getUser());
        assertEquals(admin, status.getAssignedBy());
        assertEquals("REJEITADO", status.getStatus().getName());
        assertNotNull(status.getAssignedAt());
        assertEquals(motivo, status.getRationaleForRejection());
    }

    @Test
    void rejeicaoSemMotivoDeveLancarExcecao() {
        StatusEntity statusPendente = StatusEntity.builder().id(UUID.randomUUID()).name("PENDENTE").build();
        UserStatusEntity pendente = UserStatusEntity.builder().user(usuario).status(statusPendente).assignedBy(admin).assignedAt(LocalDateTime.now().minusMinutes(10)).build();
        statusEntities.add(pendente);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, " "));
        assertTrue(ex.getMessage().toLowerCase().contains("motivo"));
    }

    @Test
    void naoPermiteAlterarStatusSeNaoForAdmin() {
        // Remove role admin do admin
        admin.setRoles(Collections.emptyList());
        StatusEntity statusPendente = StatusEntity.builder().id(UUID.randomUUID()).name("PENDENTE").build();
        UserStatusEntity pendente = UserStatusEntity.builder().user(usuario).status(statusPendente).assignedBy(admin).assignedAt(LocalDateTime.now().minusMinutes(10)).build();
        statusEntities.add(pendente);
        Exception ex = assertThrows(SecurityException.class, () ->
            service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.APROVADO, null));
        assertTrue(ex.getMessage().toLowerCase().contains("admin"));
    }

    @Test
    void naoPermiteAlterarStatusSeNaoEstiverPendente() {
        // Usuário já está aprovado
        StatusEntity statusAprovadoLocal = StatusEntity.builder().id(UUID.randomUUID()).name("APROVADO").build();
        UserStatusEntity aprovado = UserStatusEntity.builder().user(usuario).status(statusAprovadoLocal).assignedBy(admin).assignedAt(LocalDateTime.now().minusMinutes(10)).build();
        statusEntities.add(aprovado);
        Exception ex = assertThrows(IllegalStateException.class, () ->
            service.alterarStatusUsuario(usuarioId, adminId, StatusUsuario.REJEITADO, "motivo"));
        assertTrue(ex.getMessage().toLowerCase().contains("pendente"));
    }
}
