package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.LogFilterRequest;
import com.energia.backend.dto.LogResponse;
import com.energia.backend.dto.PageResponse;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.SystemLogRepository;

class SystemLogServiceTest {

    @Test
    void deveRetornarPaginaMapeandoCamposEsperados() {
        SystemLogRepository repository = mock(SystemLogRepository.class);
        SystemLogService service = new SystemLogService(repository);

        LocalDateTime timestamp = LocalDateTime.of(2026, 4, 4, 10, 30);
        SystemLog log = SystemLog.builder()
                .id(1L)
                .createdAt(timestamp)
                .actorRef("admin-1")
                .sourceType(SourceType.SYSTEM)
                .event(LogEvent.LOGIN_SUCCESS)
                .result(ResultType.SUCCESS)
                .logCategory(LogCategory.AUDIT)
                .description("Login realizado")
                .metadata("{\"ip\":\"127.0.0.1\"}")
                .build();

        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(log), PageRequest.of(0, 20), 1));

        PageResponse<LogResponse> response = service.listar(new LogFilterRequest(), 0, 20);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(timestamp, response.getContent().get(0).getTimestamp());
        assertEquals("admin-1", response.getContent().get(0).getActorRef());
        assertEquals("{\"ip\":\"127.0.0.1\"}", response.getContent().get(0).getMetadata());
    }

    @Test
    void deveRetornarNotFoundQuandoLogNaoExiste() {
        SystemLogRepository repository = mock(SystemLogRepository.class);
        SystemLogService service = new SystemLogService(repository);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Log nao encontrado.", exception.getReason());
    }
}
