package com.energia.backend.service;

import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermsJpaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsentimentoVigenteServiceTest {

    @Test
    void deveRetornarDocumentosObrigatoriosEMarketingQuandoDisponivel() {
        TermsJpaRepository repository = mock(TermsJpaRepository.class);
        ConsentimentoVigenteService service = new ConsentimentoVigenteService(repository);

        when(repository.findActiveByTypeName(eq("TERMS_OF_USE"), any(LocalDateTime.class)))
                .thenReturn(List.of(term("TERMS_OF_USE", 3, "Termos vigentes")));
        when(repository.findActiveByTypeName(eq("PRIVACY_POLICY"), any(LocalDateTime.class)))
                .thenReturn(List.of(term("PRIVACY_POLICY", 5, "Privacidade vigente")));
        when(repository.findActiveByTypeName(eq("MARKETING_COMMUNICATION"), any(LocalDateTime.class)))
                .thenReturn(List.of(term("MARKETING_COMMUNICATION", 1, "Marketing vigente")));

        ConsentimentosVigentesResponse response = service.buscarDocumentosVigentes();

        assertNotNull(response.getTerms());
        assertEquals("TERMS_OF_USE", response.getTerms().getType());
        assertEquals(3, response.getTerms().getVersion());
        assertEquals("Termos vigentes", response.getTerms().getContent());
        assertEquals(true, response.getTerms().isRequired());

        assertNotNull(response.getPrivacy());
        assertEquals("PRIVACY_POLICY", response.getPrivacy().getType());
        assertEquals(5, response.getPrivacy().getVersion());
        assertEquals("Privacidade vigente", response.getPrivacy().getContent());
        assertEquals(true, response.getPrivacy().isRequired());

        assertNotNull(response.getMarketing());
        assertEquals("MARKETING_COMMUNICATION", response.getMarketing().getType());
        assertFalse(response.getMarketing().isRequired());
    }

    @Test
    void deveRetornarMarketingNuloQuandoNaoHouverDocumentoOpcional() {
        TermsJpaRepository repository = mock(TermsJpaRepository.class);
        ConsentimentoVigenteService service = new ConsentimentoVigenteService(repository);

        when(repository.findActiveByTypeName(eq("TERMS_OF_USE"), any(LocalDateTime.class)))
                .thenReturn(List.of(term("TERMS_OF_USE", 1, "Termos vigentes")));
        when(repository.findActiveByTypeName(eq("PRIVACY_POLICY"), any(LocalDateTime.class)))
                .thenReturn(List.of(term("PRIVACY_POLICY", 1, "Privacidade vigente")));
        when(repository.findActiveByTypeName(eq("MARKETING_COMMUNICATION"), any(LocalDateTime.class)))
                .thenReturn(List.of());

        ConsentimentosVigentesResponse response = service.buscarDocumentosVigentes();

        assertNull(response.getMarketing());
    }

    @Test
    void deveFalharQuandoDocumentoObrigatorioNaoEstiverConfigurado() {
        TermsJpaRepository repository = mock(TermsJpaRepository.class);
        ConsentimentoVigenteService service = new ConsentimentoVigenteService(repository);

        when(repository.findActiveByTypeName(eq("TERMS_OF_USE"), any(LocalDateTime.class)))
                .thenReturn(List.of());

        DocumentosObrigatoriosNaoConfiguradosException exception = assertThrows(
                DocumentosObrigatoriosNaoConfiguradosException.class,
                service::buscarDocumentosVigentes
        );

        assertEquals("Documento obrigatorio nao configurado: TERMS_OF_USE.", exception.getMessage());
    }

    private TermsEntity term(String typeName, int version, String content) {
        return TermsEntity.builder()
                .id(UUID.randomUUID())
                .termType(TermTypeEntity.builder().id(UUID.randomUUID()).name(typeName).build())
                .version(version)
                .createdAt(LocalDateTime.now())
                .effectivityStartAt(LocalDateTime.now().minusDays(1))
                .content(content)
                .build();
    }
}
