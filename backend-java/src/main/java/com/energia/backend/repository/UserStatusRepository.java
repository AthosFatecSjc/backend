package com.energia.backend.repository;

import com.energia.backend.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {

    List<UserStatus> findByUserId(UUID userId);

    List<UserStatus> findByStatusId(UUID statusId);

    List<UserStatus> findByUserIdOrderByAssignedAtDesc(UUID userId);
}