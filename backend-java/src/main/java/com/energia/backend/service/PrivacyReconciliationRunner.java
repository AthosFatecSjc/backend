package com.energia.backend.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "privacy.reconciliation",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PrivacyReconciliationRunner implements ApplicationRunner {

    private final BackupRestoreReconciliationService reconciliationService;

    public PrivacyReconciliationRunner(BackupRestoreReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        reconciliationService.reconcile();
    }
}
