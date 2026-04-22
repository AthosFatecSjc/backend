package com.energia.backend.etl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.energia.backend.etl.service.AnelExtractionLoggingService;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EtlRunnerTest {

    private LogService logService;
    private AnelExtractionLoggingService loggingService;

    @BeforeEach
    void setup() {
        logService = mock(LogService.class);
        loggingService = new AnelExtractionLoggingService(logService);
    }

    @Test
    void deveLogarInicioDaExtracao() {
        loggingService.logExtractionStart();

        verify(logService, times(1)).log(
                eq("system"),
                isNull(),
                eq(SourceType.JOB),
                eq(LogEvent.ANEEL_EXTRACTION_START),
                eq(ResultType.SUCCESS),
                eq(LogCategory.TECHNICAL),
                eq("Início da extração de dados ANEEL"),
                isNull(),
                eq("aneel-etl")
        );
    }

    @Test
    void deveLogarSuccessDaExtracao() {
        String metadata = "Todos os dados ANEEL foram extraídos e carregados com sucesso";

        loggingService.logExtractionSuccess(metadata);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService).log(
                eq("system"),
                isNull(),
                eq(SourceType.JOB),
                eq(LogEvent.ANEEL_EXTRACTION_SUCCESS),
                eq(ResultType.SUCCESS),
                eq(LogCategory.TECHNICAL),
                eq("Extração de dados ANEEL finalizada com sucesso"),
                metadataCaptor.capture(),
                eq("aneel-etl")
        );

        assertEquals(metadata, metadataCaptor.getValue());
    }

    @Test
    void deveLogarDuplicatasDetectadas() {
        int quantidadeDuplicatas = 3;

        loggingService.logExtractionFailDuplicata(quantidadeDuplicatas);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService).log(
                eq("system"),
                isNull(),
                eq(SourceType.JOB),
                eq(LogEvent.ANEEL_EXTRACTION_DUPLICATES_DETECTED),
                eq(ResultType.FAIL),
                eq(LogCategory.TECHNICAL),
                eq("Dados duplicados encontrados e ignorados durante extração ANEEL"),
                metadataCaptor.capture(),
                eq("aneel-etl")
        );

        String metadata = metadataCaptor.getValue();
        assertTrue(metadata.contains("3"));
        assertTrue(metadata.contains("duplicatasIgnoradas"));
    }

    @Test
    void deveLogarFailureDaExtracao() {
        String errorMessage = "ETL finalizado com 2 erro(s): Erro ao importar distribuidoras; Erro ao importar limites";

        loggingService.logExtractionFail(errorMessage);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService).log(
                eq("system"),
                isNull(),
                eq(SourceType.JOB),
                eq(LogEvent.ANEEL_EXTRACTION_FAIL),
                eq(ResultType.FAIL),
                eq(LogCategory.TECHNICAL),
                eq("Falha na extração de dados ANEEL"),
                metadataCaptor.capture(),
                eq("aneel-etl")
        );

        String metadata = metadataCaptor.getValue();
        assertTrue(metadata.contains("erro(s)"));
    }

    @Test
    void deveAdicionarMultiplosErrosAntes() {
        loggingService.addError("Erro ao importar distribuidoras");
        loggingService.addError("Erro ao importar limites");

        assertTrue(loggingService.hasErrors());
        assertEquals(2, loggingService.getErrors().size());
    }

    @Test
    void deveNaoTerErrosQuandoNadaEhAdicionado() {
        assertFalse(loggingService.hasErrors());
        assertEquals(0, loggingService.getErrors().size());
    }

    @Test
    void deveLimparErrosAoResetar() {
        loggingService.addError("Erro 1");
        assertTrue(loggingService.hasErrors());

        loggingService.logExtractionStart();

        assertFalse(loggingService.hasErrors());
    }

    @Test
    void deveMarcarTemDuplicatasCorretamente() {
        assertFalse(loggingService.temDuplicatas());

        loggingService.logExtractionFailDuplicata(5);

        assertTrue(loggingService.temDuplicatas());
        assertEquals(5, loggingService.getDuplicatasCount());
    }

    @Test
    void deveResetarDuplicatasAoFazerNovoStart() {
        loggingService.logExtractionFailDuplicata(10);
        assertTrue(loggingService.temDuplicatas());

        loggingService.logExtractionStart();

        assertFalse(loggingService.temDuplicatas());
        assertEquals(0, loggingService.getDuplicatasCount());
    }
}
