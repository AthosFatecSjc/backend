package com.energia.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.LogFilterRequest;
import com.energia.backend.dto.LogResponse;
import com.energia.backend.dto.PageResponse;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.SystemLogRepository;
import com.energia.backend.repository.SystemLogSpecification;

@Service
public class SystemLogService {

    private final SystemLogRepository repository;
    private final LogService logService;

    public SystemLogService(SystemLogRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    public PageResponse<LogResponse> listar(LogFilterRequest filter, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var logsPage = repository.findAll(SystemLogSpecification.withFilters(filter), pageable);
        return PageResponse.from(logsPage.map(this::toResponse));
    }

    public LogResponse buscarPorId(Long id) {
        SystemLog log = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log não encontrado."));

        LogResponse response = toResponse(log);

        logService.log(
                getCurrentUserRef(),
                id.toString(),
                SourceType.USER,
                LogEvent.ADMIN_LOG_MODULE_ACCESS,
                ResultType.SUCCESS,
                LogCategory.AUDIT,
                "Acesso ao módulo administrativo de consulta de logs - detalhe",
                "operation=detail;logId=" + id,
                "SystemLogService"
        );

        return response;
    }

    private LogResponse toResponse(SystemLog log) {
        return LogResponse.builder()
                .id(log.getId())
                .createdAt(log.getCreatedAt())
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

    private String getCurrentUserRef() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        return "unknown";
    }
}
