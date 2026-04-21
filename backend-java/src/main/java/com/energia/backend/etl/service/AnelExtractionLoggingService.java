package com.energia.backend.etl.service;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.service.LogService;
import org.springframework.stereotype.Service;

@Service
public class AnelExtractionLoggingService {

    private static final String MODULE_NAME = "aneel-etl";

    private final LogService logService;

    public AnelExtractionLoggingService(LogService logService) {
        this.logService = logService;
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
        logService.log(
                "system",
                null,
                SourceType.JOB,
                LogEvent.ANEEL_EXTRACTION_FAIL,
                ResultType.FAIL,
                LogCategory.TECHNICAL,
                "Falha na extração de dados ANEEL",
                String.format("{\"error\":%s}", errorMessage == null ? "null" : '"' + errorMessage.replace("\"", "'") + '"'),
                MODULE_NAME
        );
    }
}
