package com.energia.backend.repository;

import com.energia.backend.model.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTermsRepository extends JpaRepository<UserTerms, UUID> {

    // 📄 lista tudo de um usuário
    List<UserTerms> findByUserId(UUID userId);

    // 📄 lista por termo
    List<UserTerms> findByTermsId(UUID termsId);

    // 🔒 busca ativos (não revogados)
    List<UserTerms> findByUserIdAndRevokedAtIsNull(UUID userId);

    // 🔒 verifica duplicidade ativa
    boolean existsByUserIdAndTermsIdAndRevokedAtIsNull(UUID userId, UUID termsId);

    // 🔄 importante: usado na revogação correta
    Optional<UserTerms> findByUserIdAndTermsIdAndRevokedAtIsNull(UUID userId, UUID termsId);
}