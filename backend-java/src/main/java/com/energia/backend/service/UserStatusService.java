package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;

@Service
public class UserStatusService {

    private final StatusJpaRepository statusRepository;
    private final UserStatusJpaRepository userStatusRepository;

    public UserStatusService(
            StatusJpaRepository statusRepository,
            UserStatusJpaRepository userStatusRepository
    ) {
        this.statusRepository = statusRepository;
        this.userStatusRepository = userStatusRepository;
    }

    @Transactional(readOnly = true)
    public Optional<UserStatusEntity> resolveCurrentStatusEntry(AppUserEntity user) {
        validarUsuario(user);
        return userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public StatusUsuario resolveCurrentStatus(AppUserEntity user) {
        return resolveCurrentStatusEntry(user)
                .map(UserStatusEntity::getStatus)
                .map(StatusEntity::getName)
                .map(this::toOfficialStatus)
                .orElse(null);
    }

    @Transactional
    public UserStatusEntity transitionFromPending(
            AppUserEntity user,
            AppUserEntity assignedBy,
            StatusUsuario targetStatus,
            String rejectionRationale
    ) {
        validarUsuario(user);

        if (targetStatus == null) {
            throw new IllegalArgumentException("Status desejado e obrigatorio.");
        }

        if (targetStatus != StatusUsuario.ATIVO && targetStatus != StatusUsuario.REJEITADO) {
            throw new IllegalArgumentException("Status deve ser ATIVO ou REJEITADO.");
        }

        StatusUsuario currentStatus = resolveCurrentStatus(user);
        if (currentStatus != StatusUsuario.PENDENTE) {
            throw new IllegalStateException("So e permitido ativar ou rejeitar usuarios com status PENDENTE.");
        }

        String normalizedRejectionRationale = normalizeRejectionRationale(targetStatus, rejectionRationale);
        StatusEntity targetEntity = resolveStatusEntity(targetStatus);

        UserStatusEntity nextStatus = UserStatusEntity.builder()
                .user(user)
                .status(targetEntity)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .rationaleForRejection(normalizedRejectionRationale)
                .build();

        return userStatusRepository.save(nextStatus);
    }

    public StatusUsuario toOfficialStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            return null;
        }

        try {
            return StatusUsuario.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void validarUsuario(AppUserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario e obrigatorio para operacoes de status.");
        }
    }

    private StatusEntity resolveStatusEntity(StatusUsuario status) {
        return statusRepository.findByNameIgnoreCase(status.name())
                .orElseGet(() -> statusRepository.save(
                        StatusEntity.builder().name(status.name()).build()
                ));
    }

    private String normalizeRejectionRationale(StatusUsuario targetStatus, String rejectionRationale) {
        if (targetStatus != StatusUsuario.REJEITADO) {
            return null;
        }

        if (rejectionRationale == null || rejectionRationale.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da rejeicao e obrigatorio.");
        }

        return rejectionRationale.trim();
    }
}
