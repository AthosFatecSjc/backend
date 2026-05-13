package com.energia.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.BulkEmailRequest;
import com.energia.backend.dto.BulkEmailResponse;
import com.energia.backend.service.BulkUserEmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ops/emergency/emails")
public class EmergencyEmailController {

    private final BulkUserEmailService bulkUserEmailService;
    private final String emergencyApiKey;

    public EmergencyEmailController(
            BulkUserEmailService bulkUserEmailService,
            @Value("${app.emergency.email.api-key:}") String emergencyApiKey
    ) {
        this.bulkUserEmailService = bulkUserEmailService;
        this.emergencyApiKey = emergencyApiKey;
    }

    @PostMapping("/usuarios-nao-deletados")
    public ResponseEntity<BulkEmailResponse> enviarParaUsuariosNaoDeletados(
            @RequestHeader(name = "X-Emergency-Mail-Key", required = false) String providedApiKey,
            @Valid @RequestBody BulkEmailRequest request
    ) {
        validateEmergencyKey(providedApiKey);
        BulkEmailResponse response = bulkUserEmailService.sendToAllNonDeletedUsers(
                request.getSubject(),
                request.getBody()
        );
        return ResponseEntity.ok(response);
    }

    private void validateEmergencyKey(String providedApiKey) {
        if (!StringUtils.hasText(emergencyApiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chave operacional de envio emergencial nao configurada."
            );
        }

        if (!emergencyApiKey.equals(providedApiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chave operacional invalida.");
        }
    }
}
