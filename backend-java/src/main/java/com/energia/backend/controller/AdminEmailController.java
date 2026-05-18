package com.energia.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energia.backend.dto.BulkEmailRequest;
import com.energia.backend.dto.BulkEmailResponse;
import com.energia.backend.service.BulkUserEmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/emails")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmailController {

    private final BulkUserEmailService bulkUserEmailService;

    public AdminEmailController(BulkUserEmailService bulkUserEmailService) {
        this.bulkUserEmailService = bulkUserEmailService;
    }

    @PostMapping("/usuarios-nao-deletados")
    public ResponseEntity<BulkEmailResponse> enviarParaUsuariosNaoDeletados(
            @Valid @RequestBody BulkEmailRequest request
    ) {
        BulkEmailResponse response = bulkUserEmailService.sendToAllNonDeletedUsers(
                request.getSubject(),
                request.getBody()
        );
        return ResponseEntity.ok(response);
    }
}
