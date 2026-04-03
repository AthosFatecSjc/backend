package com.energia.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.energia.backend.dto.LogFilterRequest;
import com.energia.backend.dto.LogResponse;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.SystemLogRepository;
import com.energia.backend.repository.SystemLogSpecification;

@Service
public class SystemLogService {

    private final SystemLogRepository repository;

    public SystemLogService(SystemLogRepository repository) {
        this.repository = repository;
    }

    public List<LogResponse> listar(LogFilterRequest filter) {
        return repository.findAll(SystemLogSpecification.withFilters(filter))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LogResponse buscarPorId(Long id) {
        SystemLog log = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log nao encontrado"));

        return toResponse(log);
    }

    private LogResponse toResponse(SystemLog log) {
        return LogResponse.builder()
                .id(log.getId())
                .createdAt(log.getCreatedAt())
                .sourceType(log.getSourceType())
                .event(log.getEvent())
                .result(log.getResult())
                .logCategory(log.getLogCategory())
                .description(log.getDescription())
                .build();
    }
}