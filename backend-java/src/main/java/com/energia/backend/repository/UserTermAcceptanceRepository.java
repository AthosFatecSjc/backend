package com.energia.backend.repository;

import com.energia.backend.model.UserTermAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTermAcceptanceRepository
        extends JpaRepository<UserTermAcceptance, Long> {

    Optional<UserTermAcceptance> findByUserIdAndTermId(Long userId, Long termId);
}