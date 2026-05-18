package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.energia.backend.model.privacy.ExternalUserPrivacyRecord;

public interface ExternalUserPrivacyRecordRepository
        extends MongoRepository<ExternalUserPrivacyRecord, UUID> {

    List<ExternalUserPrivacyRecord> findAllByDeletedAtIsNotNull();

    List<ExternalUserPrivacyRecord> findAllByDeletedAtIsNullAndEmailIsNotNull();

    List<ExternalUserPrivacyRecord> findAllByUserIdInAndDeletedAtIsNotNull(Collection<UUID> userIds);

    long countByDeletedAtAfter(LocalDateTime deletedAt);
}
