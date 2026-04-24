package com.energia.backend.controller;

import com.energia.backend.dto.HistoricoTermoResponse;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.UsuarioRepository;
import com.energia.backend.service.TermsUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/terms")
@RequiredArgsConstructor
public class TermsUserController {

    private final TermsUserService termsUserService;
    private final UsuarioRepository userRepository;

    @PostMapping("/approve")
    public ResponseEntity<Void> aprovarTermos(
            @PathVariable UUID userId,
            @RequestBody List<UUID> termosIds) {

        AppUserEntity user = getUserOrThrow(userId);
        termsUserService.aprovarTermos(termosIds, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revogarTermos(
            @PathVariable UUID userId,
            @RequestBody List<UUID> termosIds) {

        AppUserEntity user = getUserOrThrow(userId);
        termsUserService.revogarTermos(termosIds, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/acknowledge")
    public ResponseEntity<Void> registrarCienciaTermos(
            @PathVariable UUID userId,
            @RequestBody List<UUID> termosIds) {

        AppUserEntity user = getUserOrThrow(userId);
        termsUserService.registrarCienciaTermos(termosIds, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<HistoricoTermoResponse>> listarHistorico(
            @PathVariable UUID userId) {

        getUserOrThrow(userId);
        return ResponseEntity.ok(termsUserService.listarHistorico(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<TermsEntity>> listarTermosPendentes(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "false") Boolean apenasObrigatorios) {

        getUserOrThrow(userId);
        return ResponseEntity.ok(termsUserService.listarTermosPendentes(userId, apenasObrigatorios));
    }

    private AppUserEntity getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}