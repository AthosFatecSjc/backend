package com.energia.backend.repository;

import com.energia.backend.model.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserJpaRepository extends JpaRepository<AppUserEntity, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<AppUserEntity> findByEmailIgnoreCase(String email);
}
