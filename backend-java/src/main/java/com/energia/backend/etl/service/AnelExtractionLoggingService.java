package com.energia.backend.etl.service;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.service.LogService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnelExtractionLoggingService {

    private static final String MODULE_NAME = "aneel-etl";

    private final LogService logService;
    private final List<String> errors = new ArrayList<>();
    private int duplicatasCount = 0;
    private boolean temDuplicatas = false;

    public AnelExtractionLoggingService(LogService logService) {
        this.logService = logService;
    }

    public void logExtractionStart() {
        errors.clear();
        duplicatasCount = 0;
        temDuplicatas = false;
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
        errors.clear();
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

    public void logExtractionFailDuplicata(int quantidadeDuplicatas) {
        temDuplicatas = true;
        duplicatasCount = quantidadeDuplicatas;
        logService.log(
                "system",
                null,
                SourceType.JOB,
                LogEvent.ANEEL_EXTRACTION_DUPLICATES_DETECTED,
                ResultType.FAIL,
                LogCategory.TECHNICAL,
                "Dados duplicados encontrados e ignorados durante extração ANEEL",
                String.format("{\"duplicatasIgnoradas\":%d}", quantidadeDuplicatas),
                MODULE_NAME
        );
    }

    public void addError(String errorMessage) {
        errors.add(errorMessage);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean temDuplicatas() {
        return temDuplicatas;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public int getDuplicatasCount() {
        return duplicatasCount;
    }
}
