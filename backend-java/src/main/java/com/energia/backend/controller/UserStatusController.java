package com.energia.backend.controller;


import com.energia.backend.dto.UserStatusRequestDTO;
import com.energia.backend.service.UserStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user-status")
public class UserStatusController {

    private final UserStatusService service;

    public UserStatusController(UserStatusService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> assign(@RequestBody @Valid UserStatusRequestDTO dto) {
        return ResponseEntity.ok("Implementar criação completa depois");
    }
}