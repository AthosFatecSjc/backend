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

@RequiredArgsConstructor
class LogPersistenceService {
    private final LogRepository logRepository;
    private final LogMetadataSanitizer logMetadataSanitizer;


    @Transactional
    public void persist(@Valid LogRequest request) {
        String sanitizedMetadata = null;
        if (request.getMetadata() != null) {
            Map<String, Object> metaMap = new java.util.HashMap<>();
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                metaMap = mapper.readValue(request.getMetadata(), java.util.Map.class);
            } catch (Exception e) {
                metaMap.put("_raw", request.getMetadata());
            }
            sanitizedMetadata = null;
            try {
                sanitizedMetadata = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(logMetadataSanitizer.sanitize(metaMap));
            } catch (Exception e) {
                sanitizedMetadata = "{\"_error\":\"metadata sanitization failed\"}";
            }
        }
        SystemLog systemLog = SystemLog.builder()
                .actorRef(normalizeOptional(request.getActor()))
                .sourceType(request.getSourceType())
                .event(request.getEvent())
                .result(request.getResult())
                .logCategory(request.getLogCategory())
                .description(normalizeRequired(request.getDescription()))
                .metadata(sanitizedMetadata)
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
