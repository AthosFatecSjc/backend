package com.energia.backend.repository;

import com.energia.backend.model.UserStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserStatusJpaRepository extends JpaRepository<UserStatusEntity, UUID> {
}
