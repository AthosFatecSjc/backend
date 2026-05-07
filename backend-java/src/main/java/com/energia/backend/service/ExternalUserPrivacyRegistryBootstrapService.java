package com.energia.backend.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.model.AnonymizationStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.repository.AppUserJpaRepository;

@Component
@ConditionalOnProperty(
        prefix = "privacy.external-registry.bootstrap",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ExternalUserPrivacyRegistryBootstrapService implements ApplicationRunner {

    private final AppUserJpaRepository appUserRepository;
    private final ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService;

    public ExternalUserPrivacyRegistryBootstrapService(
            AppUserJpaRepository appUserRepository,
            ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService
    ) {
        this.appUserRepository = appUserRepository;
        this.externalUserPrivacyRegistryService = externalUserPrivacyRegistryService;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        for (AppUserEntity user : appUserRepository.findAll()) {
            if (user.getAnonymizationStatus() == AnonymizationStatus.ANONYMIZED) {
                externalUserPrivacyRegistryService.markUserDeleted(user.getId(), user.getAnonymizedAt());
                continue;
            }

            externalUserPrivacyRegistryService.upsertActiveUser(user.getId(), user.getEmail());
        }
    }
}
