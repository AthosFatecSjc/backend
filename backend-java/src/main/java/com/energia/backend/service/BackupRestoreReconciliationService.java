package com.energia.backend.service;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.privacy.PrivacyAnonymizationRegistryEntity;
import com.energia.backend.model.privacy.RestoreAction;
import com.energia.backend.repository.PrivacyAnonymizationRegistryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BackupRestoreReconciliationService {

    private static final String MODULE_NAME = "privacy-protection";

    private final PrivacyAnonymizationRegistryRepository registryRepository;
    private final UserPrivacyAnonymizationService anonymizationService;
    private final LogService logService;

    public BackupRestoreReconciliationService(
            PrivacyAnonymizationRegistryRepository registryRepository,
            UserPrivacyAnonymizationService anonymizationService,
            LogService logService
    ) {
        this.registryRepository = registryRepository;
        this.anonymizationService = anonymizationService;
        this.logService = logService;
    }

    @Transactional
    public int reconcile() {
        List<PrivacyAnonymizationRegistryEntity> registries =
                registryRepository.findAllByActiveTrueOrderByAnonymizedAtAsc();

        int reappliedCount = 0;
        for (PrivacyAnonymizationRegistryEntity registry : registries) {
            if (registry.getRestoreAction() != RestoreAction.REAPPLY) {
                continue;
            }

            if (anonymizationService.reapplyAnonymizationIfNeeded(registry)) {
                reappliedCount++;
            }
        }

        logService.log(
                "system",
                null,
                SourceType.JOB,
                LogEvent.BACKUP_RESTORE_RECONCILIATION,
                ResultType.SUCCESS,
                LogCategory.TECHNICAL,
                "Reconsolidacao de anonimizations apos restore executada.",
                "reappliedCount=" + reappliedCount + ";checkedCount=" + registries.size(),
                MODULE_NAME
        );

        return reappliedCount;
    }
}
