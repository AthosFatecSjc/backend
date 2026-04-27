package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.energia.backend.model.TermTypeName;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermTypeRepository;
import com.energia.backend.repository.TermsRepository;

class TermsServiceBuscarDocsVigentesTest {

        @Test
        void deveRetornarDocumentosObrigatoriosEMarketingQuandoDisponivel() {
                TermsRepository termsRepository = mock(TermsRepository.class);
                TermTypeRepository termTypeRepository = mock(TermTypeRepository.class);

                TermsService service = new TermsService(
                                termsRepository,
                                termTypeRepository);

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(
                                                term(TermTypeName.TERMS_OF_USE, "Termos vigentes", 1, true ),
                                                term(TermTypeName.PRIVACY_POLICY, "Privacidade vigente", 2, true),
                                                term(TermTypeName.PRIVACY_POLICY, "Privacidade vigente 2", 1, true),
                                                term(TermTypeName.MARKETING_COMMUNICATION, "Marketing vigente", 0, false)));

                ConsentimentosVigentesResponse response = service.buscarDocumentosVigentes();

                // TERMS
                assertNotNull(response.getTerms());
                assertEquals(1, response.getTerms().size());

                assertEquals("TERMS_OF_USE", response.getTerms().get(0).getType());
                assertEquals(1, response.getTerms().get(0).getClause()); 
                assertEquals("Termos vigentes", response.getTerms().get(0).getContent());
                assertTrue(response.getTerms().get(0).isRequired());

                // PRIVACY
                assertNotNull(response.getPrivacy());
                assertEquals(2, response.getPrivacy().size());

                assertEquals("PRIVACY_POLICY", response.getPrivacy().get(0).getType());
                assertEquals(2, response.getPrivacy().get(0).getClause());
                assertEquals("Privacidade vigente", response.getPrivacy().get(0).getContent());
                assertTrue(response.getPrivacy().get(0).isRequired());

                // MARKETING
                assertNotNull(response.getMarketing());
                assertEquals(1, response.getMarketing().size());
                assertEquals("MARKETING_COMMUNICATION", response.getMarketing().get(0).getType());
                assertEquals(0, response.getMarketing().get(0).getClause());
                assertEquals("Marketing vigente", response.getMarketing().get(0).getContent());
                assertFalse(response.getMarketing().get(0).isRequired());
        }

        @Test
        void deveRetornarMarketingNuloQuandoNaoHouverDocumentoOpcional() {
                TermsRepository termsRepository = mock(TermsRepository.class);
                TermTypeRepository termTypeRepository = mock(TermTypeRepository.class);
                TermsService service = new TermsService(
                                termsRepository,
                                termTypeRepository);

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(
                                        term(TermTypeName.TERMS_OF_USE, "Termos vigentes", 1, true ),
                                        term(TermTypeName.PRIVACY_POLICY, "Privacidade vigente", 2, true)));

                ConsentimentosVigentesResponse response = service.buscarDocumentosVigentes();

                assertNull(response.getMarketing());
        }

        @Test
        void deveFalharQuandoDocumentoObrigatorioNaoEstiverConfigurado() {
                TermsRepository termsRepository = mock(TermsRepository.class);
                TermTypeRepository termTypeRepository = mock(TermTypeRepository.class);
                TermsService service = new TermsService(
                                termsRepository,
                                termTypeRepository);

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(term(TermTypeName.MARKETING_COMMUNICATION, "Marketing vigente", 0, false)));

                DocumentosObrigatoriosNaoConfiguradosException exception = assertThrows(
                                DocumentosObrigatoriosNaoConfiguradosException.class,
                                service::buscarDocumentosVigentes);

                assertEquals("Documento obrigatorio nao configurado: TERMS_OF_USE", exception.getMessage());
        }

        private TermsEntity term(TermTypeName typeName, String content, Integer clause, Boolean isRequired) {
                return TermsEntity.builder()
                                .id(UUID.randomUUID())
                                .termType(
                                                TermTypeEntity.builder()
                                                                .id(UUID.randomUUID())
                                                                .isRequired(isRequired)
                                                                .name(typeName)
                                                                .build())
                                .effectivityStartAt(LocalDateTime.now().minusDays(1))
                                .content(content)
                                .clause(clause)
                                .build();
        }

}
