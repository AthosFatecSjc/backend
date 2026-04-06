package com.energia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.RoleEntity;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByNameIgnoreCase(String name);
}
