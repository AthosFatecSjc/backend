package com.energia.backend.service;

import com.energia.backend.dto.LogRequest;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.repository.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogPersistenceServiceTest {
    private LogRepository logRepository;
    private LogMetadataSanitizer sanitizer;
    private LogPersistenceService service;

    @BeforeEach
    void setup() {
        logRepository = mock(LogRepository.class);
        sanitizer = new LogMetadataSanitizer();
        service = new LogPersistenceService(logRepository, sanitizer);
    }

    @Test
    void testMetadataIsSanitized() {
        String meta = "{\"email\":\"user@email.com\",\"token\":\"abc\"}";
        LogRequest req = LogRequest.builder()
                .actor("actor")
                .sourceType(SourceType.SYSTEM)
                .event(LogEvent.USER_DELETED)
                .result(ResultType.SUCCESS)
                .logCategory(LogCategory.AUDIT)
                .description("desc")
                .metadata(meta)
                .targetRef("target")
                .module("mod")
                .build();
        service.persist(req);
        ArgumentCaptor<com.energia.backend.model.log.SystemLog> captor = ArgumentCaptor.forClass(com.energia.backend.model.log.SystemLog.class);
        verify(logRepository, times(1)).save(captor.capture());
        String sanitized = captor.getValue().getMetadata();
        assertTrue(sanitized.contains("[MASKED]"));
        assertFalse(sanitized.contains("user@email.com"));
        assertFalse(sanitized.contains("abc"));
    }

    @Test
    void testNonJsonMetadataIsHandled() {
        String meta = "raw string with password=123";
        LogRequest req = LogRequest.builder()
                .actor("actor")
                .sourceType(SourceType.SYSTEM)
                .event(LogEvent.USER_DELETED)
                .result(ResultType.SUCCESS)
                .logCategory(LogCategory.AUDIT)
                .description("desc")
                .metadata(meta)
                .targetRef("target")
                .module("mod")
                .build();
        service.persist(req);
        ArgumentCaptor<com.energia.backend.model.log.SystemLog> captor = ArgumentCaptor.forClass(com.energia.backend.model.log.SystemLog.class);
        verify(logRepository, times(1)).save(captor.capture());
        String sanitized = captor.getValue().getMetadata();
        assertTrue(sanitized.contains("[MASKED]") || sanitized.contains("_raw"));
    }
}
