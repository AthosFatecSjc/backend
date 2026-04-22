package com.energia.backend.etl.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AnelExtractionLoggingServiceTest {

    private LogService logService;
    private AnelExtractionLoggingService service;

    @BeforeEach
    void setup() {
        logService = mock(LogService.class);
        service = new AnelExtractionLoggingService(logService);
    }

    @Test
    void deveRegistrarInicioDaExtracao() {
        service.logExtractionStart();

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
    void deveRegistrarSuccessDaExtracao() {
        String metadata = "{\"registros_processados\":150,\"tabelas_atualizadas\":3}";

        service.logExtractionSuccess(metadata);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService, times(1)).log(
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
    void deveLimparErrosAoRegistrarSuccess() {
        service.addError("Erro 1");
        service.addError("Erro 2");
        assertTrue(service.hasErrors());

        service.logExtractionSuccess("metadata");

        assertFalse(service.hasErrors());
        assertEquals(0, service.getErrors().size());
    }

    @Test
    void deveRegistrarFailureDaExtracao() {
        String errorMessage = "Falha ao conectar com API ANEEL";

        service.logExtractionFail(errorMessage);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService, times(1)).log(
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
        assertTrue(metadata.contains("Falha ao conectar com API ANEEL"));
    }

    @Test
    void deveHandlerNullNoLogFailure() {
        service.logExtractionFail(null);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService, times(1)).log(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                metadataCaptor.capture(),
                any()
        );

        assertEquals("{\"error\":null}", metadataCaptor.getValue());
    }

    @Test
    void deveRegistrarDuplicatasDetectadas() {
        int quantidadeDuplicatas = 5;

        service.logExtractionFailDuplicata(quantidadeDuplicatas);

        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        verify(logService, times(1)).log(
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
        assertTrue(metadata.contains("5"));
        assertTrue(metadata.contains("duplicatasIgnoradas"));
    }

    @Test
    void deveMarcaTemDuplicatasQuandoRegistraDuplicata() {
        assertFalse(service.temDuplicatas());

        service.logExtractionFailDuplicata(3);

        assertTrue(service.temDuplicatas());
        assertEquals(3, service.getDuplicatasCount());
    }

    @Test
    void deveAdicionarErros() {
        service.addError("Erro 1");
        service.addError("Erro 2");
        service.addError("Erro 3");

        assertTrue(service.hasErrors());
        assertEquals(3, service.getErrors().size());
        assertTrue(service.getErrors().contains("Erro 1"));
        assertTrue(service.getErrors().contains("Erro 2"));
        assertTrue(service.getErrors().contains("Erro 3"));
    }

    @Test
    void deveRetornarFalseQuandoNaoTemErros() {
        assertFalse(service.hasErrors());
        assertEquals(0, service.getErrors().size());
    }

    @Test
    void deveLimparErrosAoChamarLogExtractionStart() {
        service.addError("Erro anterior");
        assertTrue(service.hasErrors());

        service.logExtractionStart();

        assertFalse(service.hasErrors());
    }

    @Test
    void deveRetornarCopiaDoListaDeErros() {
        service.addError("Erro");

        var erros1 = service.getErrors();
        var erros2 = service.getErrors();

        assertNotSame(erros1, erros2);
        assertEquals(erros1, erros2);
    }

    @Test
    void deveResetarDuplicatasAoChamarLogExtractionStart() {
        service.logExtractionFailDuplicata(10);
        assertTrue(service.temDuplicatas());
        assertEquals(10, service.getDuplicatasCount());

        service.logExtractionStart();

        assertFalse(service.temDuplicatas());
        assertEquals(0, service.getDuplicatasCount());
    }

}

