package com.energia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.energia.backend.model.TermTypeEntity;

@Repository
public interface TermTypeRepository extends JpaRepository<TermTypeEntity, UUID> {

    Optional<TermTypeEntity> findByNameIgnoreCase(String name);

}