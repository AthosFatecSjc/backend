package com.energia.backend.service;

import com.energia.backend.model.AnonymizationStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.privacy.AnonymizedEntityType;
import com.energia.backend.model.privacy.PrivacyAnonymizationRegistryEntity;
import com.energia.backend.model.privacy.RestoreAction;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.PrivacyAnonymizationRegistryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserPrivacyAnonymizationService {

    static final int STRATEGY_VERSION = 1;
    private static final String MODULE_NAME = "privacy-protection";

    private final AppUserJpaRepository appUserRepository;
    private final PrivacyAnonymizationRegistryRepository registryRepository;
    private final LogService logService;
    private final int backupRetentionDays;

    public UserPrivacyAnonymizationService(
            AppUserJpaRepository appUserRepository,
            PrivacyAnonymizationRegistryRepository registryRepository,
            LogService logService,
            @Value("${privacy.backup.retention-days:90}") int backupRetentionDays
    ) {
        this.appUserRepository = appUserRepository;
        this.registryRepository = registryRepository;
        this.logService = logService;
        this.backupRetentionDays = backupRetentionDays;
    }

    @Transactional
    public void anonymizeUser(UUID userId, String actorRef, String reason) {
        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado para anonimizar."));

        LocalDateTime now = LocalDateTime.now();
        user.clearPersonalData();
        user.setAnonymizationStatus(AnonymizationStatus.ANONYMIZED);
        user.setAnonymizedAt(now);
        appUserRepository.save(user);

        PrivacyAnonymizationRegistryEntity registry = registryRepository
                .findByEntityTypeAndEntityId(AnonymizedEntityType.APP_USER, userId)
                .orElseGet(PrivacyAnonymizationRegistryEntity::new);

        registry.setEntityType(AnonymizedEntityType.APP_USER);
        registry.setEntityId(userId);
        registry.setAnonymizedAt(now);
        registry.setAnonymizedBy(actorRef);
        registry.setReason(reason);
        registry.setStrategyVersion(STRATEGY_VERSION);
        registry.setRestoreAction(RestoreAction.REAPPLY);
        registry.setRetentionUntil(now.plusDays(backupRetentionDays));
        registry.setLastReconciledAt(now);
        registry.setLastReappliedAt(now);
        registry.setActive(true);
        registryRepository.save(registry);

        logService.log(
            actorRef,
            userId.toString(),
            SourceType.SYSTEM,
            LogEvent.USER_ANONYMIZED,
            ResultType.SUCCESS,
            LogCategory.AUDIT,
            "Anonimizacao registrada com protecao contra reativacao por restore.",
            String.format("{\"reason\":%s,\"strategyVersion\":%d}",
                reason == null ? "null" : '"' + reason.replace("\"", "'") + '"', STRATEGY_VERSION),
            MODULE_NAME
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean reapplyAnonymizationIfNeeded(PrivacyAnonymizationRegistryEntity registry) {
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
            user.setAnonymizationStatus(AnonymizationStatus.ANONYMIZED);
            user.setAnonymizedAt(now);
            appUserRepository.save(user);
            registry.setLastReappliedAt(now);

                logService.log(
                    "system",
                    registry.getEntityId().toString(),
                    SourceType.JOB,
                    LogEvent.USER_ANONYMIZATION_REAPPLIED,
                    ResultType.SUCCESS,
                    LogCategory.TECHNICAL,
                    "Anonimizacao reaplicada apos reconciliacao de restore.",
                    String.format("{\"strategyVersion\":%d}", registry.getStrategyVersion()),
                    MODULE_NAME
                );
        }

        registry.setLastReconciledAt(now);
        registryRepository.save(registry);
        return needsReapply;
    }

    boolean requiresReapply(AppUserEntity user) {
        return user.getAnonymizationStatus() != AnonymizationStatus.ANONYMIZED
                || user.getAnonymizedAt() == null
                || user.getPersonalData() != null;
    }


}
