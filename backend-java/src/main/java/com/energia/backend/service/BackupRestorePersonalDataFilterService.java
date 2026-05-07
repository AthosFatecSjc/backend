package com.energia.backend.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.energia.backend.dto.BackupUserPersonalData;

@Service
public class BackupRestorePersonalDataFilterService {

    private final ExternalUserPrivacyRegistryService privacyRegistryService;

    public BackupRestorePersonalDataFilterService(ExternalUserPrivacyRegistryService privacyRegistryService) {
        this.privacyRegistryService = privacyRegistryService;
    }

    public List<BackupUserPersonalData> filterRestorablePersonalData(
            Collection<BackupUserPersonalData> backupPersonalData
    ) {
        if (backupPersonalData == null || backupPersonalData.isEmpty()) {
            return List.of();
        }

        Set<UUID> deletedUserIds = privacyRegistryService.findDeletedUserIds(
                backupPersonalData.stream()
                        .map(BackupUserPersonalData::userId)
                        .toList()
        );

        return backupPersonalData.stream()
                .filter(data -> data.userId() != null)
                .filter(data -> !deletedUserIds.contains(data.userId()))
                .toList();
    }
}
