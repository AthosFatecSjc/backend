package com.energia.backend.etl.service;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AneelExtractionLoggingService {

    private static final String MODULE_NAME = "aneel-etl";
    private final LogService logService;
    private final ObjectMapper objectMapper;

    public AneelExtractionLoggingService(LogService logService) {
        this.logService = logService;
        this.objectMapper = new ObjectMapper();
    }

    public void logExtractionStart() {
        logService.log(
                "system",
                null,
                SourceType.JOB,
                LogEvent.ANEEL_EXTRACTION_START,
                ResultType.SUCCESS,
                LogCategory.TECHNICAL,
                "Início da extração de dados ANEEL",
                null,
                MODULE_NAME
        );
    }

    public void logExtractionSuccess(String metadata) {
        logService.log(
                "system",
                null,
                SourceType.JOB,
                LogEvent.ANEEL_EXTRACTION_SUCCESS,
                ResultType.SUCCESS,
                LogCategory.TECHNICAL,
                "Extração de dados ANEEL finalizada com sucesso",
                metadata,
                MODULE_NAME
        );
    }

    public void logExtractionFail(String errorMessage) {
        try {
            String metadata = objectMapper.writeValueAsString(Map.of("error", errorMessage));
            logService.log(
                    "system",
                    null,
                    SourceType.JOB,
                    LogEvent.ANEEL_EXTRACTION_FAIL,
                    ResultType.FAIL,
                    LogCategory.TECHNICAL,
                    "Falha na extração de dados ANEEL",
                    metadata,
                    MODULE_NAME
            );
        } catch (Exception e) {
            logService.log(
                    "system",
                    null,
                    SourceType.JOB,
                    LogEvent.ANEEL_EXTRACTION_FAIL,
                    ResultType.FAIL,
                    LogCategory.TECHNICAL,
                    "Falha na extração de dados ANEEL",
                    null,
                    MODULE_NAME
            );
        }
    }

    public void logExtractionFailDuplicata(int quantidadeDuplicatas) {
        try {
            String metadata = objectMapper.writeValueAsString(Map.of("duplicatasIgnoradas", quantidadeDuplicatas));
            logService.log(
                    "system",
                    null,
                    SourceType.JOB,
                    LogEvent.ANEEL_EXTRACTION_DUPLICATES_DETECTED,
                    ResultType.FAIL,
                    LogCategory.TECHNICAL,
                    "Dados duplicados encontrados e ignorados durante extração ANEEL",
                    metadata,
                    MODULE_NAME
            );
        } catch (Exception e) {
            logService.log(
                    "system",
                    null,
                    SourceType.JOB,
                    LogEvent.ANEEL_EXTRACTION_DUPLICATES_DETECTED,
                    ResultType.FAIL,
                    LogCategory.TECHNICAL,
                    "Dados duplicados encontrados e ignorados durante extração ANEEL",
                    null,
                    MODULE_NAME
            );
        }
    }
}
