package com.energia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.energia.backend.model.AppUserEntity;

public interface UsuarioRepository extends JpaRepository<AppUserEntity, UUID> {
    @Query("select u from AppUserEntity u join fetch u.personalData personalData where lower(personalData.email) = lower(?1)")
    Optional<AppUserEntity> findByEmail(String email);
    Optional<AppUserEntity> findById(UUID id);

    @Query("select case when count(u) > 0 then true else false end from AppUserEntity u join u.personalData personalData where lower(personalData.email) = lower(?1)")
    Boolean existsByEmail(String email);

    @Query("select case when count(u) > 0 then true else false end from AppUserEntity u join u.personalData personalData where lower(personalData.email) = lower(?1)")
    boolean existsByEmailIgnoreCase(String email);
}
