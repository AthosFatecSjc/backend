package com.energia.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.energia.backend.dto.LogRequest;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.LogRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servico centralizado para registro de eventos e auditoria do sistema.
 * Substitui operacoes CRUD genericas por um contrato de registro padronizado.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    @Transactional
    public void log(@Valid LogRequest request) {
        SystemLog systemLog = SystemLog.builder()
                .actorRef(normalizeOptional(request.getActor()))
                .sourceType(request.getSourceType())
                .event(request.getEvent())
                .result(request.getResult())
                .logCategory(request.getLogCategory())
                .description(normalizeRequired(request.getDescription()))
                .metadata(normalizeOptional(request.getMetadata()))
                .targetRef(normalizeOptional(request.getTargetRef()))
                .createdByModule(normalizeRequired(request.getModule()))
                .build();

        try {
            logRepository.save(systemLog);
        } catch (Exception e) {
            log.error(
                    "Falha ao persistir log de sistema. event={}, sourceType={}, result={}, category={}, module={}",
                    request.getEvent(),
                    request.getSourceType(),
                    request.getResult(),
                    request.getLogCategory(),
                    request.getModule(),
                    e);
        }
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
