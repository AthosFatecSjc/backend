package com.energia.backend.service;

import com.energia.backend.model.DeletionStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.privacy.DeletedEntityType;
import com.energia.backend.model.privacy.PrivacyDeletionRegistryEntity;
import com.energia.backend.model.privacy.RestoreAction;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.PrivacyDeletionRegistryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserPrivacyDeletionService {

    static final int STRATEGY_VERSION = 1;
    private static final String MODULE_NAME = "privacy-protection";

    private final AppUserJpaRepository appUserRepository;
    private final PrivacyDeletionRegistryRepository registryRepository;
    private final ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService;
    private final LogService logService;
    private final int backupRetentionDays;

    public UserPrivacyDeletionService(
            AppUserJpaRepository appUserRepository,
            PrivacyDeletionRegistryRepository registryRepository,
            ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService,
            LogService logService,
            @Value("${privacy.backup.retention-days:90}") int backupRetentionDays
    ) {
        this.appUserRepository = appUserRepository;
        this.registryRepository = registryRepository;
        this.externalUserPrivacyRegistryService = externalUserPrivacyRegistryService;
        this.logService = logService;
        this.backupRetentionDays = backupRetentionDays;
    }

    @Transactional
    public void deleteUser(UUID userId, String actorRef, String reason) {
        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado para deletar."));

        LocalDateTime now = LocalDateTime.now();
        user.clearPersonalData();
        user.setDeletionStatus(DeletionStatus.DELETED);
        user.setDeletedAt(now);
        appUserRepository.save(user);

        PrivacyDeletionRegistryEntity registry = registryRepository
                .findByEntityTypeAndEntityId(DeletedEntityType.APP_USER, userId)
                .orElseGet(PrivacyDeletionRegistryEntity::new);

        registry.setEntityType(DeletedEntityType.APP_USER);
        registry.setEntityId(userId);
        registry.setDeletedAt(now);
        registry.setDeletedBy(actorRef);
        registry.setReason(reason);
        registry.setStrategyVersion(STRATEGY_VERSION);
        registry.setRestoreAction(RestoreAction.REAPPLY);
        registry.setRetentionUntil(now.plusDays(backupRetentionDays));
        registry.setLastReconciledAt(now);
        registry.setLastReappliedAt(now);
        registry.setActive(true);
        registryRepository.save(registry);
        externalUserPrivacyRegistryService.markUserDeleted(userId, now);

        logService.log(
            actorRef,
            userId.toString(),
            SourceType.SYSTEM,
            LogEvent.USER_DELETED,
            ResultType.SUCCESS,
            LogCategory.AUDIT,
            "Delecao registrada com protecao contra reativacao por restore.",
            String.format("{\"reason\":%s,\"strategyVersion\":%d}",
                reason == null ? "null" : '"' + reason.replace("\"", "'") + '"', STRATEGY_VERSION),
            MODULE_NAME
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean reapplyDeletionIfNeeded(PrivacyDeletionRegistryEntity registry) {
        Optional<AppUserEntity> maybeUser = appUserRepository.findById(registry.getEntityId());
        LocalDateTime now = LocalDateTime.now();

        if (maybeUser.isEmpty()) {
            registry.setLastReconciledAt(now);
            registryRepository.save(registry);
            return false;
        }

        AppUserEntity user = maybeUser.get();
        boolean needsReapply = requiresReapply(user);

        if (needsReapply) {
            user.clearPersonalData();
            user.setDeletionStatus(DeletionStatus.DELETED);
            user.setDeletedAt(now);
            appUserRepository.save(user);
            registry.setLastReappliedAt(now);

                logService.log(
                    "system",
                    registry.getEntityId().toString(),
                    SourceType.JOB,
                    LogEvent.USER_DELETION_REAPPLIED,
                    ResultType.SUCCESS,
                    LogCategory.TECHNICAL,
                    "Delecao reaplicada apos reconciliacao de restore.",
                    String.format("{\"strategyVersion\":%d}", registry.getStrategyVersion()),
                    MODULE_NAME
                );
        }

        registry.setLastReconciledAt(now);
        registryRepository.save(registry);
        return needsReapply;
    }

    boolean requiresReapply(AppUserEntity user) {
        return user.getDeletionStatus() != DeletionStatus.DELETED
                || user.getDeletedAt() == null
                || user.getPersonalData() != null;
    }


}
