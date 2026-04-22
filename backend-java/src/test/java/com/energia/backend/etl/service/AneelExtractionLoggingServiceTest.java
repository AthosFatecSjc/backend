package com.energia.backend.etl.service;

import static org.mockito.Mockito.*;

import com.energia.backend.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AneelExtractionLoggingServiceTest {

    private LogService logService;
    private AneelExtractionLoggingService service;

    @BeforeEach
    void setup() {
        logService = mock(LogService.class);
        service = new AneelExtractionLoggingService(logService);
    }

    @Test
    void deveRegistrarInicioDaExtracao() {
        service.logExtractionStart();
        verify(logService).log(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveRegistrarSuccessDaExtracao() {
        service.logExtractionSuccess("metadata");
        verify(logService).log(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveRegistrarFailureDaExtracao() {
        service.logExtractionFail("erro");
        verify(logService).log(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveRegistrarDuplicatasDetectadas() {
        service.logExtractionFailDuplicata(5);
        verify(logService).log(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
