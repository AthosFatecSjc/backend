package com.energia.backend.repository;

import com.energia.backend.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByNameIgnoreCase(String name);
}
