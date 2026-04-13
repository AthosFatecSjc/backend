package com.energia.backend.service;

import com.energia.backend.dto.InternalAneelJobLogRequest;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InternalAneelJobLogServiceTest {

    @Test
    void deveRegistrarEventoStartComCamposPadronizados() {
        LogService logService = mock(LogService.class);
        InternalAneelJobLogService service = new InternalAneelJobLogService(logService, "internal-key");

        InternalAneelJobLogRequest request = new InternalAneelJobLogRequest();
        request.setEvent(LogEvent.ANEEL_EXTRACTION_START);
        request.setResult(ResultType.SUCCESS);
        request.setDescription("Inicio da rotina");
        request.setMetadata("routine=import_aneel_perdas;stage=start");
        request.setTargetRef("exec-123");

        service.log(request, "internal-key");

        verify(logService).log(
                eq("system"),
                eq("exec-123"),
                eq(SourceType.JOB),
                eq(LogEvent.ANEEL_EXTRACTION_START),
                eq(ResultType.SUCCESS),
                eq(LogCategory.TECHNICAL),
                eq("Inicio da rotina"),
                eq("routine=import_aneel_perdas,stage=start"),
                eq("aneel-etl-python")
        );
    }

    @Test
    void deveRegistrarEventoSuccessComCamposPadronizados() {
        LogService logService = mock(LogService.class);
        InternalAneelJobLogService service = new InternalAneelJobLogService(logService, "internal-key");

        InternalAneelJobLogRequest request = new InternalAneelJobLogRequest();
        request.setEvent(LogEvent.ANEEL_EXTRACTION_SUCCESS);
        request.setResult(ResultType.SUCCESS);
        request.setDescription("Conclusao com sucesso");
        request.setMetadata("routine=import_aneel_perdas;stage=completed;raw_rows=10;loaded_rows=8");
        request.setTargetRef("exec-124");

        service.log(request, "internal-key");

        verify(logService).log(
                eq("system"),
                eq("exec-124"),
                eq(SourceType.JOB),
                eq(LogEvent.ANEEL_EXTRACTION_SUCCESS),
                eq(ResultType.SUCCESS),
                eq(LogCategory.TECHNICAL),
                eq("Conclusao com sucesso"),
                eq("routine=import_aneel_perdas,stage=completed,raw_rows=10,loaded_rows=8"),
                eq("aneel-etl-python")
        );
    }

    @Test
    void deveRegistrarEventoFailComCamposPadronizados() {
        LogService logService = mock(LogService.class);
        InternalAneelJobLogService service = new InternalAneelJobLogService(logService, "internal-key");

        InternalAneelJobLogRequest request = new InternalAneelJobLogRequest();
        request.setEvent(LogEvent.ANEEL_EXTRACTION_FAIL);
        request.setResult(ResultType.FAIL);
        request.setDescription("Falha na rotina");
        request.setMetadata("routine=import_aneel_perdas;stage=failed;error=timeout");
        request.setTargetRef("exec-125");

        service.log(request, "internal-key");

        verify(logService).log(
                eq("system"),
                eq("exec-125"),
                eq(SourceType.JOB),
                eq(LogEvent.ANEEL_EXTRACTION_FAIL),
                eq(ResultType.FAIL),
                eq(LogCategory.TECHNICAL),
                eq("Falha na rotina"),
                eq("routine=import_aneel_perdas,stage=failed,error=timeout"),
                eq("aneel-etl-python")
        );
    }

    @Test
    void deveFalharQuandoChaveInternaForInvalida() {
        LogService logService = mock(LogService.class);
        InternalAneelJobLogService service = new InternalAneelJobLogService(logService, "internal-key");

        InternalAneelJobLogRequest request = new InternalAneelJobLogRequest();
        request.setEvent(LogEvent.ANEEL_EXTRACTION_START);
        request.setResult(ResultType.SUCCESS);
        request.setDescription("Inicio da rotina");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.log(request, "wrong-key")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void deveFalharQuandoEventoNaoForPermitido() {
        LogService logService = mock(LogService.class);
        InternalAneelJobLogService service = new InternalAneelJobLogService(logService, "internal-key");

        InternalAneelJobLogRequest request = new InternalAneelJobLogRequest();
        request.setEvent(LogEvent.LOGIN_SUCCESS);
        request.setResult(ResultType.SUCCESS);
        request.setDescription("Evento invalido");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.log(request, "internal-key")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
