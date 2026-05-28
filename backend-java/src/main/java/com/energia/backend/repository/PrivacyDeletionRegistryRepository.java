package com.energia.backend.repository;

import com.energia.backend.model.privacy.DeletedEntityType;
import com.energia.backend.model.privacy.PrivacyDeletionRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrivacyDeletionRegistryRepository
        extends JpaRepository<PrivacyDeletionRegistryEntity, UUID> {

    Optional<PrivacyDeletionRegistryEntity> findByEntityTypeAndEntityId(
            DeletedEntityType entityType,
            UUID entityId
    );

    List<PrivacyDeletionRegistryEntity> findAllByActiveTrueOrderByDeletedAtAsc();
}
