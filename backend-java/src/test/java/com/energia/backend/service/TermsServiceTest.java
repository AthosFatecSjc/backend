package com.energia.backend.service;

import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TermsServiceTest {

        @Test
        void deveRejeitarQuandoNaoRecebeTodosOsTermosObrigatoriosVigentes() {
                TermsRepository termsRepository = mock(TermsRepository.class);
                UserTermsRepository userTermsRepository = mock(UserTermsRepository.class);
                TermsUserService service = new TermsUserService(
                                termsRepository,
                                userTermsRepository);

                TermsEntity marketingEntity = termo("MARKETING_COMMUNICATION", 1, false);
                TermsEntity termosDeUsoVigente = termo("TERMS_OF_USE", 2, true);
                TermsEntity politicaPrivacidadeVigente = termo("PRIVACY_POLICY", 3, true);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(marketingEntity));
                when(termsRepository.findActiveRequiredByReferenceTime(any()))
                                .thenReturn(List.of(termosDeUsoVigente, politicaPrivacidadeVigente));

                DocumentosObrigatoriosNaoConfiguradosException exception = assertThrows(
                                DocumentosObrigatoriosNaoConfiguradosException.class,
                                () -> service.aprovarTermos(List.of(marketingEntity.getTermType().getName()),
                                                new AppUserEntity()));

                assertEquals("Termo obrigatorio vigente nao aceito", exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void deveRejeitarQuandoRecebeVersaoAntigaDeTermoObrigatorio() {
                TermsRepository termsRepository = mock(TermsRepository.class);
                UserTermsRepository userTermsRepository = mock(UserTermsRepository.class);
                TermsUserService termsUserService = new TermsUserService(
                                termsRepository,
                                userTermsRepository);

                TermsEntity termosDeUsoAntigoEntity = termo("TERMS_OF_USE", 1, true);
                TermsEntity termosDeUsoVigente = termo("TERMS_OF_USE", 2, true);
                TermsEntity politicaPrivacidadeVigenteEntity = termo("PRIVACY_POLICY", 1, true);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(termosDeUsoAntigoEntity, politicaPrivacidadeVigenteEntity));
                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(termosDeUsoVigente, politicaPrivacidadeVigenteEntity));

                TermoNaoEncontradoException exception = assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.aprovarTermos(
                                                List.of(
                                                        termosDeUsoAntigoEntity.getTermType().getName(),
                                                        politicaPrivacidadeVigenteEntity.getTermType().getName()
                                                ),
                                                new AppUserEntity()));

                assertEquals("Termo enviado nao é vigente: PRIVACY_POLICY", exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void devePersistirAceitesQuandoRecebeOsTermosObrigatoriosVigentes() {
                TermsRepository termsRepository = mock(TermsRepository.class);
                UserTermsRepository userTermsRepository = mock(UserTermsRepository.class);
                TermsUserService termsUserService = new TermsUserService(
                                termsRepository,
                                userTermsRepository);

                TermsEntity termosDeUsoVigenteEntity = termo("TERMS_OF_USE", 2, true);
                TermsEntity politicaPrivacidadeVigenteEntity = termo("PRIVACY_POLICY", 1, true);

                when(termsRepository.findAllWithTypeByIdIn(
                                List.of(termosDeUsoVigenteEntity.getId(), politicaPrivacidadeVigenteEntity.getId())))
                                .thenReturn(List.of(termosDeUsoVigenteEntity, politicaPrivacidadeVigenteEntity));
                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(termosDeUsoVigenteEntity, politicaPrivacidadeVigenteEntity));
                when(userTermsRepository.findHistoryByUserId(any(UUID.class))).thenReturn(List.of());

                termsUserService.aprovarTermos(
                                List.of(termosDeUsoVigenteEntity.getTermType().getName(),
                                                politicaPrivacidadeVigenteEntity.getTermType().getName()),
                                new AppUserEntity());

                verify(userTermsRepository, times(2)).save(any());
        }

        @Test
        void deveRejeitarQuandoRecebeTermoInexistente() {
                TermsRepository termsRepository = mock(TermsRepository.class);
                UserTermsRepository userTermsRepository = mock(UserTermsRepository.class);
                TermsUserService termsUserService = new TermsUserService(
                                termsRepository,
                                userTermsRepository);

                TermsEntity nonExistentTermEntity = termo("NON_EXISTENT_TERM", 1, true);

                when(termsRepository.findAllWithTypeByIdIn(List.of(nonExistentTermEntity.getId())))
                                .thenReturn(List.of());

                TermoNaoEncontradoException exception = assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.aprovarTermos(
                                                List.of(nonExistentTermEntity.getTermType().getName()),
                                                new AppUserEntity()));

                assertEquals("Um ou mais termos informados nao existem.", exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        private TermsEntity termo(String typeName, int version, boolean required) {
                TermTypeEntity tipo = new TermTypeEntity();
                tipo.setName(typeName);
                tipo.setIsRequired(true);

                TermsEntity termo = new TermsEntity();
                termo.setId(UUID.randomUUID());
                termo.setTermType(tipo);
                termo.setVersion(version);
                termo.setEffectivityStartAt(LocalDateTime.now());
                termo.setContent(typeName);
                return termo;
        }
}