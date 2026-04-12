package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermTypeRepository;
import com.energia.backend.repository.TermsJpaRepository;
import com.energia.backend.repository.TermsRepository;

class TermsServiceBuscarDocsVigentesTest {

    @Test
    void deveRetornarDocumentosObrigatoriosEMarketingQuandoDisponivel() {
        TermsRepository termsRepository = mock(TermsRepository.class);
        TermsJpaRepository termsJpaRepository = mock(TermsJpaRepository.class);
        TermTypeRepository termTypeRepository = mock(TermTypeRepository.class);
        TermsService service = new TermsService(
                termsRepository,
                termsJpaRepository,
                termTypeRepository
        );

        when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
        .thenReturn(List.of(
                term("TERMS_OF_USE", 3, "Termos vigentes", true),
                term("PRIVACY_POLICY", 5, "Privacidade vigente", true),
                term("MARKETING_COMMUNICATION", 1, "Marketing vigente", false)
        ));

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
        TermsRepository termsRepository = mock(TermsRepository.class);
        TermsJpaRepository termsJpaRepository = mock(TermsJpaRepository.class);
        TermTypeRepository termTypeRepository = mock(TermTypeRepository.class);
        TermsService service = new TermsService(
                termsRepository,
                termsJpaRepository,
                termTypeRepository
        );

        when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
        .thenReturn(List.of(
                term("TERMS_OF_USE", 3, "Termos vigentes", false),
                term("PRIVACY_POLICY", 5, "Privacidade vigente", false)
        ));

        ConsentimentosVigentesResponse response = service.buscarDocumentosVigentes();

        assertNull(response.getMarketing());
    }

    @Test
    void deveFalharQuandoDocumentoObrigatorioNaoEstiverConfigurado() {
        TermsRepository termsRepository = mock(TermsRepository.class);
        TermsJpaRepository termsJpaRepository = mock(TermsJpaRepository.class);
        TermTypeRepository termTypeRepository = mock(TermTypeRepository.class);
        TermsService service = new TermsService(
                termsRepository,
                termsJpaRepository,
                termTypeRepository
        );

        when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
        .thenReturn(List.of());
        
        DocumentosObrigatoriosNaoConfiguradosException exception = assertThrows(
                DocumentosObrigatoriosNaoConfiguradosException.class,
                service::buscarDocumentosVigentes
        );

        assertEquals("Documento obrigatorio nao configurado: TERMS_OF_USE", exception.getMessage());
    }

    private TermsEntity term(String typeName, int version, String content, Boolean isRequired) {
        return TermsEntity.builder()
                .id(UUID.randomUUID())
                .termType(
                        TermTypeEntity.builder()
                        .id(UUID.randomUUID())
                        .isRequired(isRequired)
                        .name(typeName)
                        .build()
                )
                .version(version)
                .effectivityStartAt(LocalDateTime.now().minusDays(1))
                .content(content)
                .build();
    }

}
