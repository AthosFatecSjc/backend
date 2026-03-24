package com.energia.backend.service;

import com.energia.backend.model.AppUser;
import com.energia.backend.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AppUserService {

    private final AppUserRepository repository;

    public AppUserService(AppUserRepository repository) {
        this.repository = repository;
    }

    public AppUser create(AppUser user) {
        

        if (repository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        return repository.save(user);
    }

    public AppUser findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public AppUser findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}