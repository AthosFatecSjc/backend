package com.energia.backend.controller;

import com.energia.backend.dto.TermoRequest;
import com.energia.backend.dto.TermosResponse;
import com.energia.backend.service.TermsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @PostMapping
    public ResponseEntity<TermosResponse> cadastrarNovoTermo(
            @RequestBody TermoRequest request) {

        TermosResponse termo = termsService.cadastrarNovoTermo(request);
        return ResponseEntity.ok(termo);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<TermosResponse> desativarTermo(
            @PathVariable UUID id) {

        TermosResponse termo = termsService.desativarTermo(id);
        return ResponseEntity.ok(termo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TermosResponse> editarTermo(
            @PathVariable UUID id,
            @RequestBody TermoRequest request) {

        TermosResponse termo = termsService.editarTermo(id, request);
        return ResponseEntity.ok(termo);
    }
}