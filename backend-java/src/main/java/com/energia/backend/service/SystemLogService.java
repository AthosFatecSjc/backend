package com.energia.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.energia.backend.dto.LogFilterRequest;
import com.energia.backend.dto.LogResponse;
import com.energia.backend.dto.PageResponse;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.SystemLogRepository;
import com.energia.backend.repository.SystemLogSpecification;

@Service
public class SystemLogService {

    private final SystemLogRepository repository;

    public SystemLogService(SystemLogRepository repository) {
        this.repository = repository;
    }

    public PageResponse<LogResponse> listar(LogFilterRequest filter, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var logsPage = repository.findAll(SystemLogSpecification.withFilters(filter), pageable);

        return PageResponse.from(logsPage.map(this::toResponse));
    }

    public LogResponse buscarPorId(Long id) {
        SystemLog log = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log nao encontrado."));

        return toResponse(log);
    }

    private LogResponse toResponse(SystemLog log) {
        return LogResponse.builder()
                .id(log.getId())
                .timestamp(log.getCreatedAt())
                .actorRef(log.getActorRef())
                .targetRef(log.getTargetRef())
                .sourceType(log.getSourceType())
                .event(log.getEvent())
                .result(log.getResult())
                .logCategory(log.getLogCategory())
                .description(log.getDescription())
                .createdByModule(log.getCreatedByModule())
                .metadata(log.getMetadata())
                .build();
    }
}
