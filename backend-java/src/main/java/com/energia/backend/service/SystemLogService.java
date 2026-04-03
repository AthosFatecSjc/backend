package com.energia.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.energia.backend.dto.LogFilterRequest;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.SystemLogRepository;
import com.energia.backend.repository.SystemLogSpecification;



@Service
public class SystemLogService {

    private final SystemLogRepository repository;

    public SystemLogService(SystemLogRepository repository) {
        this.repository = repository;
    }

    public List<SystemLog> listar(LogFilterRequest filter) {
        return repository.findAll(SystemLogSpecification.withFilters(filter));
    }

    public SystemLog buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log nao encontrado"));
    }
}