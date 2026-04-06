package com.energia.backend.repository;

import com.energia.backend.model.privacy.AnonymizedEntityType;
import com.energia.backend.model.privacy.PrivacyAnonymizationRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrivacyAnonymizationRegistryRepository
        extends JpaRepository<PrivacyAnonymizationRegistryEntity, UUID> {

    Optional<PrivacyAnonymizationRegistryEntity> findByEntityTypeAndEntityId(
            AnonymizedEntityType entityType,
            UUID entityId
    );

    List<PrivacyAnonymizationRegistryEntity> findAllByActiveTrueOrderByAnonymizedAtAsc();
}
