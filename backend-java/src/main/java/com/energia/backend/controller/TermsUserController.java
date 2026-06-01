package com.energia.backend.controller;

import com.energia.backend.dto.TermosResponse;
import com.energia.backend.dto.UserTermResponse;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.service.TermsUserService;
import com.energia.backend.service.UsuarioCadastroService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/terms")
@RequiredArgsConstructor
public class TermsUserController {

    private final TermsUserService termsUserService;
    private final UsuarioCadastroService cadastroService;
    private final TermsRepository termsRepository;

    @PostMapping("/approve")
    public ResponseEntity<Void> aprovarTermos(
            @PathVariable UUID userId,
            @RequestBody List<UUID> termosIds) {

        AppUserEntity user = cadastroService.getUserOrThrow(userId);
        termsUserService.aprovarTermos(termosIds, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revogarTermos(
            @PathVariable UUID userId,
            @RequestBody List<UUID> termosIds) {

        AppUserEntity user = cadastroService.getUserOrThrow(userId);
        termsUserService.revogarTermos(termosIds, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/acknowledge")
    public ResponseEntity<Void> registrarCienciaTermos(
            @PathVariable UUID userId,
            @RequestBody List<UUID> termosIds) {

        AppUserEntity user = cadastroService.getUserOrThrow(userId);
        termsUserService.registrarCienciaTermos(termosIds, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<UserTermResponse>> listarHistorico(
            @PathVariable UUID userId) {

        cadastroService.getUserOrThrow(userId);
        return ResponseEntity.ok(termsUserService.listarHistorico(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<TermosResponse>> listarTermosPendentes(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "false") Boolean apenasObrigatorios) {

        cadastroService.getUserOrThrow(userId);
        return ResponseEntity.ok(
                termsUserService.listarTermosPendentes(userId, apenasObrigatorios)
                        .stream()
                        .map(TermosResponse::fromEntity)
                        .toList());
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<TermosResponse>> listarTermosVigentesAceitos(
            @PathVariable UUID userId)
            {
        
        cadastroService.getUserOrThrow(userId);
        List<TermsEntity> vigentesAceitos = termsRepository.findAcceptedLatestTermsByUser(userId, LocalDateTime.now());

        return ResponseEntity.ok(
                vigentesAceitos
                        .stream()
                        .map(TermosResponse::fromEntity)
                        .toList());
    }

   

}