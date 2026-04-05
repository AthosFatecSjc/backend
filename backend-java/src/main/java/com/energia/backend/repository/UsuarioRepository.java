package com.energia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.AppUserEntity;

public interface UsuarioRepository extends JpaRepository<AppUserEntity, UUID> {
    Optional<AppUserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
    
}
