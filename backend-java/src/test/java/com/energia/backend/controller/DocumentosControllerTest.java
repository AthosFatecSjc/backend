package com.energia.backend.controller;

import com.energia.backend.dto.ConsentimentoDocumentoResponse;
import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.service.TermsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentosControllerTest {

    @Test
    void deveRetornarConsentimentosVigentes() {
        TermsService service = mock(TermsService.class);
        DocumentosController controller = new DocumentosController(service);

        ConsentimentosVigentesResponse payload = new ConsentimentosVigentesResponse(
                new ConsentimentoDocumentoResponse(UUID.randomUUID(), "TERMS_OF_USE", 1, "Termos", true),
                new ConsentimentoDocumentoResponse(UUID.randomUUID(), "PRIVACY_POLICY", 1, "Privacidade", true),
                null
        );

        when(service.buscarDocumentosVigentes()).thenReturn(payload);

        ResponseEntity<ConsentimentosVigentesResponse> response = controller.listarConsentimentosVigentes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TERMS_OF_USE", response.getBody().getTerms().getType());
        assertEquals("PRIVACY_POLICY", response.getBody().getPrivacy().getType());
        assertEquals(null, response.getBody().getMarketing());
    }
}
