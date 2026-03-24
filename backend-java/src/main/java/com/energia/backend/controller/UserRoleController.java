package com.energia.backend.controller;

import com.energia.backend.dto.UserRoleRequestDTO;
import com.energia.backend.service.UserRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user-roles")
public class UserRoleController {

    private final UserRoleService service;

    public UserRoleController(UserRoleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> assign(@RequestBody @Valid UserRoleRequestDTO dto) {
        // Aqui você pode montar o UserRole via service depois
        return ResponseEntity.ok("Implementar criação com entity completa");
    }
}