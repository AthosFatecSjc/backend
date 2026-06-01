package com.energia.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.energia.backend.dto.BulkEmailRequest;
import com.energia.backend.dto.BulkEmailResponse;
import com.energia.backend.service.BulkUserEmailService;

class AdminEmailControllerTest {

    @Test
    void deveDelegarEnvioParaServicoDeEmailEmMassa() {
        BulkUserEmailService service = mock(BulkUserEmailService.class);
        AdminEmailController controller = new AdminEmailController(service);
        BulkEmailRequest request = new BulkEmailRequest();
        request.setSubject("Comunicado de seguranca");
        request.setBody("Mensagem para usuarios.");

        BulkEmailResponse expected = new BulkEmailResponse(
                2,
                2,
                0,
                "Envio concluido para todos os usuarios nao deletados."
        );
        when(service.sendToAllNonDeletedUsers(request.getSubject(), request.getBody())).thenReturn(expected);

        ResponseEntity<BulkEmailResponse> response = controller.enviarParaUsuariosNaoDeletados(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(service).sendToAllNonDeletedUsers("Comunicado de seguranca", "Mensagem para usuarios.");
    }
}
