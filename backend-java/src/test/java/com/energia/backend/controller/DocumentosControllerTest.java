package com.energia.backend.controller;

import com.energia.backend.dto.ConsentimentoDocumentoResponse;
import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.service.TermsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
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
                List.of(new ConsentimentoDocumentoResponse(UUID.randomUUID(), "TERMS_OF_USE", "termos de uso", true, 1)),
                List.of(new ConsentimentoDocumentoResponse(UUID.randomUUID(), "PRIVACY_POLICY", "política de privacidade", true, 1)),
                null
        );

        when(service.buscarDocumentosVigentes()).thenReturn(payload);

        ResponseEntity<ConsentimentosVigentesResponse> response = controller.listarConsentimentosVigentes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TERMS_OF_USE", response.getBody().getTerms().get(0).getType());
        assertEquals("PRIVACY_POLICY", response.getBody().getPrivacy().get(0).getType());
        assertEquals(null, response.getBody().getMarketing());
    }
}
