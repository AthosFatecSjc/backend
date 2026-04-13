package com.energia.backend.controller;

import com.energia.backend.dto.InternalAneelJobLogRequest;
import com.energia.backend.service.InternalAneelJobLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/jobs/aneel/logs")
public class InternalAneelJobLogController {

    private final InternalAneelJobLogService service;

    public InternalAneelJobLogController(InternalAneelJobLogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestHeader(name = "X-Internal-Log-Key", required = false) String internalLogKey,
            @Valid @RequestBody InternalAneelJobLogRequest request
    ) {
        service.log(request, internalLogKey);
        return ResponseEntity.accepted().build();
    }
}
