package com.energia.backend.service;

import com.energia.backend.dto.TermoRequest;
import com.energia.backend.dto.TermosResponse;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermTypeName;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermTypeRepository;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TermsServiceCadastroTest {

        private TermsRepository termsRepository;
        private TermTypeRepository termTypeRepository;
        private UserTermsRepository userTermsRepository;
        private TermsService service;

        @BeforeEach
        void setUp() {
                termsRepository = mock(TermsRepository.class);
                termTypeRepository = mock(TermTypeRepository.class);
                userTermsRepository = mock(UserTermsRepository.class);

                service = new TermsService(
                                termsRepository,
                                termTypeRepository,
                                userTermsRepository);
        }

        @Test
        void deveCriarPrimeiroTermoQuandoNaoExisteNenhum() {

                TermoRequest request = new TermoRequest();
                request.setTermTypeName("TERMS_OF_USE");
                request.setContent("conteudo");

                TermTypeEntity type = new TermTypeEntity();
                type.setId(UUID.randomUUID());
                type.setName(TermTypeName.TERMS_OF_USE);

                when(termTypeRepository.findByName(TermTypeName.TERMS_OF_USE))
                                .thenReturn(Optional.of(type));

                when(termsRepository.findMaxClauseByTermType(type.getId()))
                                .thenReturn(0);

                when(termsRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                TermosResponse result = service.cadastrarNovoTermo(request);

                assertEquals(1, result.clause());
                assertEquals("conteudo", result.content());
                assertEquals(type.getName().name(), result.typeName());
                assertNotNull(result.effectivityStartAt());
                assertNull(result.effectivityEndAt());

                verify(termsRepository).save(any());
        }

        @Test
        void deveIncrementarClauseQuandoJaExiste() {

                TermoRequest request = new TermoRequest();
                request.setTermTypeName("PRIVACY_POLICY");
                request.setContent("conteudo");

                TermTypeEntity type = new TermTypeEntity();
                type.setId(UUID.randomUUID());
                type.setName(TermTypeName.PRIVACY_POLICY);

                when(termTypeRepository.findByName(TermTypeName.PRIVACY_POLICY))
                                .thenReturn(Optional.of(type));

                when(termsRepository.findMaxClauseByTermType(type.getId()))
                                .thenReturn(3);

                when(termsRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                TermosResponse result = service.cadastrarNovoTermo(request);

                assertEquals(4, result.clause());
        }

        @Test
        void deveUsarEffectivityStartAtDoRequestQuandoInformado() {

                LocalDateTime inicio = LocalDateTime.now().minusDays(10);

                TermoRequest request = new TermoRequest();
                request.setTermTypeName("TERMS_OF_USE");
                request.setContent("conteudo");
                request.setEffectivityStartAt(inicio);

                TermTypeEntity type = new TermTypeEntity();
                type.setId(UUID.randomUUID());
                type.setName(TermTypeName.TERMS_OF_USE);

                when(termTypeRepository.findByName(any()))
                                .thenReturn(Optional.of(type));

                when(termsRepository.findMaxClauseByTermType(any()))
                                .thenReturn(0);

                when(termsRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                TermosResponse result = service.cadastrarNovoTermo(request);

                assertEquals(inicio, result.effectivityStartAt());
        }

        @Test
        void deveUsarDataAtualQuandoEffectivityNaoInformado() {

                TermoRequest request = new TermoRequest();
                request.setTermTypeName("TERMS_OF_USE");
                request.setContent("conteudo");

                TermTypeEntity type = new TermTypeEntity();
                type.setId(UUID.randomUUID());
                type.setName(TermTypeName.TERMS_OF_USE); // ✅ necessário

                when(termTypeRepository.findByName(any()))
                                .thenReturn(Optional.of(type));

                when(termsRepository.findMaxClauseByTermType(any()))
                                .thenReturn(0);

                when(termsRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                TermosResponse result = service.cadastrarNovoTermo(request);

                assertNotNull(result.effectivityStartAt());
        }
}