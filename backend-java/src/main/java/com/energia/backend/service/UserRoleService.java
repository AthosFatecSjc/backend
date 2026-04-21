package com.energia.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.RoleJpaRepository;

@Service
public class UserRoleService {

    private final AppUserJpaRepository appUserRepository;
    private final RoleJpaRepository roleRepository;
    private final UserStatusService userStatusService;

    public UserRoleService(
            AppUserJpaRepository appUserRepository,
            RoleJpaRepository roleRepository,
            UserStatusService userStatusService
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userStatusService = userStatusService;
    }

    @Transactional
    public void alterarRoleUsuario(UUID usuarioId, UUID adminId, String roleName) {
        // Validar que o admin é um admin
        AppUserEntity admin = appUserRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado."));

        boolean isAdmin = admin.getRoles() != null
                && admin.getRoles().stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
        if (!isAdmin) {
            throw new PermissaoNegadaException("Apenas administradores podem alterar roles de usuários.");
        }

        // Validar que o usuário existe
        AppUserEntity usuario = appUserRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // Validar que o usuário tem status ATIVO
        StatusUsuario currentStatus = userStatusService.resolveCurrentStatus(usuario);
        if (currentStatus != StatusUsuario.ATIVO) {
            throw new IllegalStateException("Apenas usuários com status ATIVO podem ter sua role alterada.");
        }

        // Validar que a role existe
        RoleEntity role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role não encontrada: " + roleName));

        // Limpar roles atuais e adicionar a nova
        usuario.getRoles().clear();
        usuario.getRoles().add(role);

        appUserRepository.save(usuario);
    }
}
