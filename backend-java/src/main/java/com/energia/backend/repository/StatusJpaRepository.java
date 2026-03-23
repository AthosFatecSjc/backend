package com.energia.backend.repository;

import com.energia.backend.model.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatusJpaRepository extends JpaRepository<StatusEntity, UUID> {
    Optional<StatusEntity> findByNameIgnoreCase(String name);
}
