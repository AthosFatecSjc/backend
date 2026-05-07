package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.energia.backend.model.privacy.ExternalUserPrivacyRecord;
import com.energia.backend.repository.ExternalUserPrivacyRecordRepository;

@Service
public class ExternalUserPrivacyRegistryService {

    private final ExternalUserPrivacyRecordRepository repository;

    public ExternalUserPrivacyRegistryService(ExternalUserPrivacyRecordRepository repository) {
        this.repository = repository;
    }

    public void upsertActiveUser(UUID userId, String email) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required for LGPD registry.");
        }

        LocalDateTime now = LocalDateTime.now();
        ExternalUserPrivacyRecord record = repository.findById(userId)
                .orElseGet(() -> ExternalUserPrivacyRecord.builder()
                        .userId(userId)
                        .createdAt(now)
                        .build());

        record.setEmail(normalizeEmail(email));
        record.setDeletedAt(null);
        record.setUpdatedAt(now);
        repository.save(record);
    }

    public void markUserDeleted(UUID userId, LocalDateTime deletedAt) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required for LGPD registry.");
        }

        LocalDateTime now = LocalDateTime.now();
        ExternalUserPrivacyRecord record = repository.findById(userId)
                .orElseGet(() -> ExternalUserPrivacyRecord.builder()
                        .userId(userId)
                        .createdAt(now)
                        .build());

        record.setEmail(null);
        record.setDeletedAt(Optional.ofNullable(deletedAt).orElse(now));
        record.setUpdatedAt(now);
        repository.save(record);
    }

    public Set<UUID> findDeletedUserIds(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();
        }

        return repository.findAllByUserIdInAndDeletedAtIsNotNull(userIds).stream()
                .map(ExternalUserPrivacyRecord::getUserId)
                .collect(Collectors.toSet());
    }

    public Set<UUID> findAllDeletedUserIds() {
        return repository.findAllByDeletedAtIsNotNull().stream()
                .map(ExternalUserPrivacyRecord::getUserId)
                .collect(Collectors.toSet());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
