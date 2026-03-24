package com.energia.backend.repository;

import com.energia.backend.model.TermType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TermTypeRepository extends JpaRepository<TermType, UUID> {

    Optional<TermType> findByName(String name);
}