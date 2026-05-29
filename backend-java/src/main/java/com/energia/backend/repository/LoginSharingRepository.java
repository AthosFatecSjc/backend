package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.energia.backend.model.LoginSharingRequestEntity;
import com.energia.backend.model.LoginSharingStatus;

@Repository
public interface LoginSharingRepository extends JpaRepository<LoginSharingRequestEntity, UUID> {
    
    List<LoginSharingRequestEntity> findByUser_Id(UUID userId);
    
    List<LoginSharingRequestEntity> findByUser_IdAndStatus(UUID userId, LoginSharingStatus status);
    
    Optional<LoginSharingRequestEntity> findByIdAndUser_Id(UUID requestId, UUID userId);
    
    List<LoginSharingRequestEntity> findByStatusAndExpiresAtBefore(LoginSharingStatus status, LocalDateTime expiresAt);
}
