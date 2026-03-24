package com.energia.backend.controller;

import com.energia.backend.dto.UserTermsRequestDTO;
import com.energia.backend.dto.UserTermsResponseDTO;
import com.energia.backend.model.UserTerms;
import com.energia.backend.service.UserTermsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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

    @PostMapping
    public ResponseEntity<UserTermsResponseDTO> accept(@RequestBody @Valid UserTermsRequestDTO dto) {

        UserTerms saved = service.acceptTerms(
                dto.getUserId(),
                dto.getTermsId(),
                dto.getAcceptedFrom()
        );

        return ResponseEntity.ok(
                UserTermsResponseDTO.builder()
                        .id(saved.getId())
                        .userId(saved.getUser().getId())
                        .termsId(saved.getTerms().getId())
                        .acceptedAt(saved.getAcceptedAt())
                        .acceptedFrom(saved.getAcceptedFrom())
                        .revokedAt(saved.getRevokedAt())
                        .build()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserTermsResponseDTO>> getByUser(@PathVariable UUID userId) {

        List<UserTermsResponseDTO> response = service.getUserTerms(userId)
                .stream()
                .map(t -> UserTermsResponseDTO.builder()
                        .id(t.getId())
                        .userId(t.getUser().getId())
                        .termsId(t.getTerms().getId())
                        .acceptedAt(t.getAcceptedAt())
                        .acceptedFrom(t.getAcceptedFrom())
                        .revokedAt(t.getRevokedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/{termsId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID userId,
                                       @PathVariable UUID termsId) {

        service.revokeTerms(userId, termsId);
        return ResponseEntity.noContent().build();
    }
}