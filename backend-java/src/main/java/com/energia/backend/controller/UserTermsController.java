package com.energia.backend.controller;

import com.energia.backend.dto.UserTermsRequestDTO;
import com.energia.backend.dto.UserTermsResponseDTO;
import com.energia.backend.model.UserTerms;
import com.energia.backend.service.UserTermsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-terms")
public class UserTermsController {

    private final UserTermsService service;

    public UserTermsController(UserTermsService service) {
        this.service = service;
    }

    // ✅ ACEITAR TERMOS
    @PostMapping
    public ResponseEntity<UserTermsResponseDTO> accept(
            @RequestBody @Valid UserTermsRequestDTO dto,
            HttpServletRequest request) {

        String ip = extractClientIp(request);

        UserTerms saved = service.acceptTerms(
                dto.getUserId(),
                dto.getTermsId(),
                dto.getAcceptedFrom(),
                ip
        );

        return ResponseEntity.ok(toResponse(saved));
    }

    // 📄 LISTAR TERMOS DO USUÁRIO
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserTermsResponseDTO>> getByUser(@PathVariable UUID userId) {

        List<UserTermsResponseDTO> response = service.getUserTerms(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 🔄 REVOGAR TERMO
    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(
            @RequestParam UUID userId,
            @RequestParam UUID termsId) {

        service.revokeTerms(userId, termsId);
        return ResponseEntity.ok().build();
    }

    // =========================
    // 🔧 helpers
    // =========================

    private UserTermsResponseDTO toResponse(UserTerms t) {
        return UserTermsResponseDTO.builder()
                .id(t.getId())
                .userId(t.getUser().getId())
                .termsId(t.getTerms().getId())
                .acceptedAt(t.getAcceptedAt())
                .acceptedFrom(t.getAcceptedFrom())
                .acceptedFromIp(t.getAcceptedFromIp())
                .accepted(t.getAccepted())
                .revokedAt(t.getRevokedAt())
                .build();
    }

    // 🌐 pega IP real do cliente
    private String extractClientIp(HttpServletRequest request) {

        String header = request.getHeader("X-Forwarded-For");

        if (header != null && !header.isEmpty()) {
            return header.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}