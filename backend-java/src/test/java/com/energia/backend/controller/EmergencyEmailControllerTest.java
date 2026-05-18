package com.energia.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.BulkEmailRequest;
import com.energia.backend.dto.BulkEmailResponse;
import com.energia.backend.service.BulkUserEmailService;

class EmergencyEmailControllerTest {

    @Test
    void deveEnviarQuandoChaveOperacionalForValida() {
        BulkUserEmailService service = mock(BulkUserEmailService.class);
        EmergencyEmailController controller = new EmergencyEmailController(service, "secret-key");
        BulkEmailRequest request = new BulkEmailRequest();
        request.setSubject("Incidente");
        request.setBody("Mensagem");
        BulkEmailResponse expected = new BulkEmailResponse(1, 1, 0, "ok");

        when(service.sendToAllNonDeletedUsers("Incidente", "Mensagem")).thenReturn(expected);

        ResponseEntity<BulkEmailResponse> response = controller.enviarParaUsuariosNaoDeletados(
                "secret-key",
                request
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(service).sendToAllNonDeletedUsers("Incidente", "Mensagem");
    }

    @Test
    void deveBloquearQuandoChaveOperacionalEstiverAusenteOuInvalida() {
        BulkUserEmailService service = mock(BulkUserEmailService.class);
        EmergencyEmailController controller = new EmergencyEmailController(service, "secret-key");
        BulkEmailRequest request = new BulkEmailRequest();
        request.setSubject("Incidente");
        request.setBody("Mensagem");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.enviarParaUsuariosNaoDeletados("wrong-key", request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void deveIndicarServicoIndisponivelQuandoChaveNaoEstiverConfigurada() {
        BulkUserEmailService service = mock(BulkUserEmailService.class);
        EmergencyEmailController controller = new EmergencyEmailController(service, "");
        BulkEmailRequest request = new BulkEmailRequest();
        request.setSubject("Incidente");
        request.setBody("Mensagem");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.enviarParaUsuariosNaoDeletados("qualquer", request)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }
}
