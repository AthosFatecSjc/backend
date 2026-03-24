package com.energia.backend.service;

import com.energia.backend.model.UserStatus;
import com.energia.backend.repository.UserStatusRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserStatusService {

    private final UserStatusRepository repository;

    public UserStatusService(UserStatusRepository repository) {
        this.repository = repository;
    }

    public UserStatus assignStatus(UserStatus status) {
        status.setAssignedAt(LocalDateTime.now());
        return repository.save(status);
    }

    public List<UserStatus> getUserStatus(UUID userId) {
        return repository.findByUserIdOrderByAssignedAtDesc(userId);
    }
}