package com.energia.backend.service;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.privacy.PrivacyDeletionRegistryEntity;
import com.energia.backend.model.privacy.RestoreAction;
import com.energia.backend.repository.PrivacyDeletionRegistryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackupRestoreReconciliationServiceTest {

    @Test
    void deveContinuarProcessandoQuandoUmaReaplicacaoFalhar() {
        PrivacyDeletionRegistryRepository registryRepository =
                mock(PrivacyDeletionRegistryRepository.class);
        UserPrivacyDeletionService deletionService = mock(UserPrivacyDeletionService.class);
        LogService logService = mock(LogService.class);
        BackupRestoreReconciliationService service = new BackupRestoreReconciliationService(
                registryRepository,
                deletionService,
                logService
        );

        PrivacyDeletionRegistryEntity failedRegistry = PrivacyDeletionRegistryEntity.builder()
                .entityId(UUID.randomUUID())
                .restoreAction(RestoreAction.REAPPLY)
                .active(true)
                .build();
        PrivacyDeletionRegistryEntity successfulRegistry = PrivacyDeletionRegistryEntity.builder()
                .entityId(UUID.randomUUID())
                .restoreAction(RestoreAction.REAPPLY)
                .active(true)
                .build();

        when(registryRepository.findAllByActiveTrueOrderByDeletedAtAsc())
                .thenReturn(List.of(failedRegistry, successfulRegistry));
        when(deletionService.reapplyDeletionIfNeeded(failedRegistry))
                .thenThrow(new IllegalStateException("registro inconsistente"));
        when(deletionService.reapplyDeletionIfNeeded(successfulRegistry))
                .thenReturn(true);

        int reappliedCount = service.reconcile();

        assertEquals(1, reappliedCount);
        verify(deletionService).reapplyDeletionIfNeeded(failedRegistry);
        verify(deletionService).reapplyDeletionIfNeeded(successfulRegistry);
        verify(logService).log(
                eq("system"),
                eq(failedRegistry.getEntityId().toString()),
                eq(SourceType.JOB),
                eq(LogEvent.BACKUP_RESTORE_RECONCILIATION),
                eq(ResultType.FAIL),
                eq(LogCategory.TECHNICAL),
                any(),
                any(),
                eq("privacy-protection")
        );
        verify(logService).log(
                eq("system"),
                eq(null),
                eq(SourceType.JOB),
                eq(LogEvent.BACKUP_RESTORE_RECONCILIATION),
                eq(ResultType.FAIL),
                eq(LogCategory.TECHNICAL),
                any(),
                eq("reappliedCount=1;failedCount=1;checkedCount=2"),
                eq("privacy-protection")
        );
        verify(logService, times(2)).log(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }
}
