package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.energia.backend.dto.BackupUserPersonalData;

class BackupRestorePersonalDataFilterServiceTest {

    @Test
    void deveRemoverDadosPessoaisDeUsuariosDeletadosAntesDoRestore() {
        ExternalUserPrivacyRegistryService registryService = mock(ExternalUserPrivacyRegistryService.class);
        BackupRestorePersonalDataFilterService service =
                new BackupRestorePersonalDataFilterService(registryService);

        UUID activeUserId = UUID.randomUUID();
        UUID deletedUserId = UUID.randomUUID();

        List<BackupUserPersonalData> backupData = List.of(
                new BackupUserPersonalData(activeUserId, "Maria", "maria@teste.com", "11999999999"),
                new BackupUserPersonalData(deletedUserId, "Joao", "joao@teste.com", "11888888888")
        );

        when(registryService.findDeletedUserIds(anyCollection())).thenReturn(Set.of(deletedUserId));

        List<BackupUserPersonalData> filtered = service.filterRestorablePersonalData(backupData);

        assertEquals(1, filtered.size());
        assertEquals(activeUserId, filtered.get(0).userId());
        verify(registryService).findDeletedUserIds(List.of(activeUserId, deletedUserId));
    }
}
