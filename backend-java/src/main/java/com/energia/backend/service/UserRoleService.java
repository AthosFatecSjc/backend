package com.energia.backend.service;

import com.energia.backend.model.UserRole;
import com.energia.backend.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserRoleService {

    private final UserRoleRepository repository;

    public UserRoleService(UserRoleRepository repository) {
        this.repository = repository;
    }

    public UserRole assignRole(UserRole userRole) {
        if (repository.existsByUserIdAndRoleId(
                userRole.getUser().getId(),
                userRole.getRole().getId()
        )) {
            throw new RuntimeException("Usuário já possui essa role");
        }

        return repository.save(userRole);
    }

    public List<UserRole> getRolesByUser(UUID userId) {
        return repository.findByUserId(userId);
    }
}