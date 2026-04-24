package com.energia.backend.controller;

import com.energia.backend.dto.TermoRequest;
import com.energia.backend.model.TermsEntity;
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
    public ResponseEntity<TermsEntity> cadastrarNovoTermo(
            @RequestBody TermoRequest request) {

        TermsEntity termo = termsService.cadastrarNovoTermo(request);
        return ResponseEntity.ok(termo);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<TermsEntity> desativarTermo(
            @PathVariable UUID id) {

        TermsEntity termo = termsService.desativarTermo(id);
        return ResponseEntity.ok(termo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TermsEntity> editarTermo(
            @PathVariable UUID id,
            @RequestBody TermoRequest request) {

        TermsEntity termo = termsService.editarTermo(id, request);
        return ResponseEntity.ok(termo);
    }
}