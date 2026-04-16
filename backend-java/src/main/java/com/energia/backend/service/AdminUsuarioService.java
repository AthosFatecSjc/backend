package com.energia.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.AdminUsuarioResponse;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;

@Service
public class AdminUsuarioService {

    private final AppUserJpaRepository appUserRepository;
    private final UserStatusService userStatusService;

    public AdminUsuarioService(
            AppUserJpaRepository appUserRepository,
            UserStatusService userStatusService
    ) {
        this.appUserRepository = appUserRepository;
        this.userStatusService = userStatusService;
    }

    @Transactional(readOnly = true)
    public List<AdminUsuarioResponse> listarUsuarios() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private AdminUsuarioResponse toResponse(AppUserEntity user) {
        StatusUsuario currentStatus = userStatusService.resolveCurrentStatus(user);
        String role = resolveRole(user);

        return new AdminUsuarioResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                currentStatus != null ? currentStatus.name() : StatusUsuario.PENDENTE.name(),
                role,
                user.getCreatedAt()
        );
    }

    private String resolveRole(AppUserEntity user) {
        boolean isAdmin = user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));

        return isAdmin ? "ADMIN" : "USUARIO";
    }
}
