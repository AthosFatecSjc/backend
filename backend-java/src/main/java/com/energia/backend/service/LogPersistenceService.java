package com.energia.backend.service;

import com.energia.backend.dto.LogRequest;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.LogRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class LogPersistenceService {

    private final LogRepository logRepository;

    @Transactional
    public void persist(@Valid LogRequest request) {
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
