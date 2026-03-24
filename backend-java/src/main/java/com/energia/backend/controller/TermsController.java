package com.energia.backend.controller;

import com.energia.backend.dto.TermsRequestDTO;
import com.energia.backend.dto.TermsResponseDTO;
import com.energia.backend.model.Terms;
import com.energia.backend.service.TermsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/terms")
public class TermsController {

    private final TermsService service;

    public TermsController(TermsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TermsResponseDTO> create(@RequestBody @Valid TermsRequestDTO dto) {

        Terms terms = Terms.builder()
                .version(dto.getVersion())
                .content(dto.getContent())
                .effectivityStartAt(dto.getEffectivityStartAt())
                .effectivityEndAt(dto.getEffectivityEndAt())
                .createdAt(LocalDateTime.now())
                .build();

        Terms saved = service.create(terms);

        return ResponseEntity.ok(
                TermsResponseDTO.builder()
                        .id(saved.getId())
                        .version(saved.getVersion())
                        .content(saved.getContent())
                        .effectivityStartAt(saved.getEffectivityStartAt())
                        .effectivityEndAt(saved.getEffectivityEndAt())
                        .createdAt(saved.getCreatedAt())
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<TermsResponseDTO>> findActive() {

        List<TermsResponseDTO> response = service.findActiveTerms()
                .stream()
                .map(t -> TermsResponseDTO.builder()
                        .id(t.getId())
                        .version(t.getVersion())
                        .content(t.getContent())
                        .effectivityStartAt(t.getEffectivityStartAt())
                        .effectivityEndAt(t.getEffectivityEndAt())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}