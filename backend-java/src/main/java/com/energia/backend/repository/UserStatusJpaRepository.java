package com.energia.backend.repository;

import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.model.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStatusJpaRepository extends JpaRepository<UserStatusEntity, UUID> {
	Optional<UserStatusEntity> findFirstByUserOrderByAssignedAtDesc(AppUserEntity user);

	Optional<UserStatusEntity> findFirstByUserOrderByAssignedAtAsc(AppUserEntity user);
}
