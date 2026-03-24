package com.energia.backend.repository;

import com.energia.backend.model.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserTermsRepository extends JpaRepository<UserTerms, UUID> {

    List<UserTerms> findByUserId(UUID userId);

    List<UserTerms> findByTermsId(UUID termsId);

    List<UserTerms> findByUserIdAndRevokedAtIsNull(UUID userId);

    boolean existsByUserIdAndTermsIdAndRevokedAtIsNull(UUID userId, UUID termsId);
}