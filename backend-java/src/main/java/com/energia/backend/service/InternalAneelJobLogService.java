package com.energia.backend.service;

import com.energia.backend.dto.InternalAneelJobLogRequest;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.SourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class InternalAneelJobLogService {

    private static final String MODULE_NAME = "aneel-etl-python";
    private static final Set<LogEvent> ALLOWED_EVENTS = Set.of(
            LogEvent.ANEEL_EXTRACTION_START,
            LogEvent.ANEEL_EXTRACTION_SUCCESS,
            LogEvent.ANEEL_EXTRACTION_FAIL
    );

    private final LogService logService;
    private final String internalLogApiKey;

    public InternalAneelJobLogService(
            LogService logService,
            @Value("${INTERNAL_LOG_API_KEY:}") String internalLogApiKey
    ) {
        this.logService = logService;
        this.internalLogApiKey = internalLogApiKey;
    }

    public void log(InternalAneelJobLogRequest request, String providedApiKey) {
        validateApiKey(providedApiKey);
        validateEvent(request.getEvent());

        logService.log(
                "system",
                request.getTargetRef(),
                SourceType.JOB,
                request.getEvent(),
                request.getResult(),
                LogCategory.TECHNICAL,
                sanitizeText(request.getDescription(), 1000),
                sanitizeText(request.getMetadata(), 4000),
                MODULE_NAME
        );
    }

    private void validateApiKey(String providedApiKey) {
        if (internalLogApiKey == null || internalLogApiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "INTERNAL_LOG_API_KEY nao configurada"
            );
        }

        if (providedApiKey == null || !internalLogApiKey.equals(providedApiKey.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chave interna invalida");
        }
    }

    private void validateEvent(LogEvent event) {
        if (!ALLOWED_EVENTS.contains(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evento nao permitido para integracao ANEEL");
        }
    }

    private String sanitizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String sanitized = value
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replace(";", ",")
                .trim();

        if (sanitized.isEmpty()) {
            return null;
        }

        return sanitized.length() <= maxLength
                ? sanitized
                : sanitized.substring(0, maxLength);
    }
}
