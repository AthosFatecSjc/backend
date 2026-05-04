package com.energia.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.energia.backend.dto.LogRequest;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Servico centralizado para registro de eventos e auditoria do sistema.
 * Substitui operacoes CRUD genericas por um contrato de registro padronizado.
 */
@Service
@Validated
@RequiredArgsConstructor
public class LogService {

    private final LogPersistenceService logPersistenceService;

    public void log(@Valid LogRequest request) {
        logPersistenceService.persist(request);
    }

    public void log(
            String actorRef,
            String targetRef,
            SourceType sourceType,
            LogEvent event,
            ResultType result,
            LogCategory category,
            String description,
            String metadata,
            String createdByModule
    ) {
        LogRequest logRequest = LogRequest.builder()
                .actor(actorRef)
                .targetRef(targetRef)
                .sourceType(sourceType)
                .event(event)
                .result(result)
                .logCategory(category)
                .description(description)
                .metadata(metadata)
                .module(createdByModule)
                .build();

        System.out.println("LogService.log: " + logRequest); // Log para depuração
        logPersistenceService.persist(logRequest);
    }
}
