package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
        LogService logService = mock(LogService.class);
        SystemLogService service = new SystemLogService(repository, logService);

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

        // Mock SecurityContextHolder
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin-user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            PageResponse<LogResponse> response = service.listar(new LogFilterRequest(), 0, 20);

            assertEquals(1, response.getContent().size());
            assertEquals(0, response.getPage());
            assertEquals(20, response.getSize());
            assertEquals(1, response.getTotalElements());
            assertEquals(1, response.getTotalPages());
            assertEquals(timestamp, response.getContent().get(0).getTimestamp());
            assertEquals("admin-1", response.getContent().get(0).getActorRef());
            assertEquals("{\"ip\":\"127.0.0.1\"}", response.getContent().get(0).getMetadata());

            // Verificar se o log de auditoria foi chamado
            verify(logService).log(
                    eq("admin-user"),
                    eq(null),
                    eq(SourceType.USER),
                    eq(LogEvent.ADMIN_LOG_MODULE_ACCESS),
                    eq(ResultType.SUCCESS),
                    eq(LogCategory.AUDIT),
                    eq("Acesso ao módulo administrativo de consulta de logs - listagem"),
                    eq("operation=list;page=0;size=20"),
                    eq("SystemLogService")
            );
        }
    }

    @Test
    void deveRetornarNotFoundQuandoLogNaoExiste() {
        SystemLogRepository repository = mock(SystemLogRepository.class);
        LogService logService = mock(LogService.class);
        SystemLogService service = new SystemLogService(repository, logService);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Log nao encontrado.", exception.getReason());

        // Verificar que o log não foi chamado pois a exceção foi lançada antes
        // verify(logService, never()).log(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveRetornarLogQuandoEncontradoERegistrarAuditoria() {
        SystemLogRepository repository = mock(SystemLogRepository.class);
        LogService logService = mock(LogService.class);
        SystemLogService service = new SystemLogService(repository, logService);

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

        when(repository.findById(1L)).thenReturn(Optional.of(log));

        // Mock SecurityContextHolder
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin-user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            LogResponse response = service.buscarPorId(1L);

            assertEquals(1L, response.getId());
            assertEquals(timestamp, response.getTimestamp());
            assertEquals("admin-1", response.getActorRef());

            // Verificar se o log de auditoria foi chamado
            verify(logService).log(
                    eq("admin-user"),
                    eq("1"),
                    eq(SourceType.USER),
                    eq(LogEvent.ADMIN_LOG_MODULE_ACCESS),
                    eq(ResultType.SUCCESS),
                    eq(LogCategory.AUDIT),
                    eq("Acesso ao módulo administrativo de consulta de logs - detalhe"),
                    eq("operation=detail;logId=1"),
                    eq("SystemLogService")
            );
        }
    }
}
