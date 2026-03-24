package com.energia.backend.controller;


import com.energia.backend.dto.AppUserRequestDTO;
import com.energia.backend.dto.AppUserResponseDTO;
import com.energia.backend.model.AppUser;
import com.energia.backend.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService service;

    public AppUserController(AppUserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AppUserResponseDTO> create(@RequestBody @Valid AppUserRequestDTO dto) {

        AppUser user = AppUser.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .phone(dto.getPhone())
                .build();

        AppUser saved = service.create(user);

        return ResponseEntity.ok(
                AppUserResponseDTO.builder()
                        .id(saved.getId())
                        .name(saved.getName())
                        .email(saved.getEmail())
                        .phone(saved.getPhone())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponseDTO> findById(@PathVariable UUID id) {

        AppUser user = service.findById(id);

        return ResponseEntity.ok(
                AppUserResponseDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build()
        );
    }
}