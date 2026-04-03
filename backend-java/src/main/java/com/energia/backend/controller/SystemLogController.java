package com.energia.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.energia.backend.dto.LogFilterRequest;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.service.SystemLogService;

@RestController
@RequestMapping("/admin/logs")
public class SystemLogController {

    private final SystemLogService service;

    public SystemLogController(SystemLogService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SystemLog>> listar(LogFilterRequest filter) {
        return ResponseEntity.ok(service.listar(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemLog> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}