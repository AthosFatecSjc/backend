package com.energia.backend.controller;

import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.service.ConsentimentoVigenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documentos")
public class DocumentosController {
    private final ConsentimentoVigenteService consentimentoVigenteService;

    public DocumentosController(ConsentimentoVigenteService consentimentoVigenteService) {
        this.consentimentoVigenteService = consentimentoVigenteService;
    }

    @GetMapping("/consentimentos/vigentes")
    public ResponseEntity<ConsentimentosVigentesResponse> listarConsentimentosVigentes() {
        return ResponseEntity.ok(consentimentoVigenteService.buscarDocumentosVigentes());
    }
}
