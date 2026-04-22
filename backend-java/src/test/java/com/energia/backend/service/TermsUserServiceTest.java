package com.energia.backend.service;

import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.exception.NenhumTermoPassadoException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermTypeName;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.UserTermsAction;
import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.repository.TermTypeRepository;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRepository;
import com.energia.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TermsUserServiceTest {

        private TermsRepository termsRepository;
        private UserTermsRepository userTermsRepository;
        private UsuarioRepository userRepository;
        private TermsService termsService;
        private TermsUserService termsUserService;

        @BeforeEach
        void setUp() {
                termsRepository = mock(TermsRepository.class);
                userTermsRepository = mock(UserTermsRepository.class);
                userRepository = mock(UsuarioRepository.class);
                termsService = new TermsService(
                                termsRepository,
                                mock(TermTypeRepository.class),
                                userTermsRepository);
                termsUserService = new TermsUserService(
                                termsRepository,
                                userTermsRepository,
                                termsService,
                                userRepository);
        }

        @Test
        void aprovarTermos_deveFalharQuandoListaForNulaOuVazia() {
                AppUserEntity user = new AppUserEntity();

                assertThrows(NenhumTermoPassadoException.class,
                                () -> termsUserService.aprovarTermos(null, user));

                assertThrows(NenhumTermoPassadoException.class,
                                () -> termsUserService.aprovarTermos(List.of(), user));

                verifyNoInteractions(termsRepository);
                verifyNoInteractions(userTermsRepository);
        }

        @Test
        void aprovarTermos_deveFalharQuandoAlgumTermoNaoExistir() {
                UUID termoId = UUID.randomUUID();

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of());

                TermoNaoEncontradoException exception = assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.aprovarTermos(List.of(termoId), new AppUserEntity()));

                assertTrue(exception.getMessage().startsWith("Um ou mais termos informados nao existem."));
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void aprovarTermos_deveFalharQuandoTermoEnviadoNaoForVigente() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                TermTypeEntity optionalType = termType(UUID.randomUUID(), "PRIVACY_POLICY", false);
                TermsEntity notActiveTerm = term(UUID.randomUUID(), optionalType, 1, "outro", true);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(requiredTerm, notActiveTerm));

                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(userTermsRepository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                                .thenReturn(List.of());

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(requiredTerm, notActiveTerm));

                TermoNaoEncontradoException exception = assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.aprovarTermos(
                                                List.of(requiredTerm.getId(), notActiveTerm.getId()), user));

                assertEquals("Termo enviado nao é vigente",
                                exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void aprovarTermos_deveFalharQuandoTermoObrigatorioNaoFoiAceito() {

                AppUserEntity user = new AppUserEntity();

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                // usuário NÃO envia o obrigatório
                TermsEntity outroTermo = term(
                                UUID.randomUUID(),
                                termType(UUID.randomUUID(), "PRIVACY_POLICY", false),
                                1,
                                "outro",
                                true);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(outroTermo));

                // existe um termo obrigatório vigente
                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm, outroTermo));

                when(userTermsRepository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                                .thenReturn(List.of());

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(outroTermo));

                DocumentosObrigatoriosNaoConfiguradosException exception = assertThrows(
                                DocumentosObrigatoriosNaoConfiguradosException.class,
                                () -> termsUserService.aprovarTermos(List.of(outroTermo.getId()), user));

                assertEquals("Termo obrigatório está pendente de aceite", exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void aprovarTermos_deveSalvarQuandoTudoEstiverCorreto() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(userTermsRepository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                                .thenReturn(List.of());

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(requiredTerm));

                termsUserService.aprovarTermos(List.of(requiredTerm.getId()), user);

                ArgumentCaptor<UserTermsEntity> captor = ArgumentCaptor.forClass(UserTermsEntity.class);
                verify(userTermsRepository, times(1)).save(captor.capture());

                UserTermsEntity saved = captor.getValue();
                assertEquals(user, saved.getUser());
                assertEquals(requiredTerm, saved.getTerms());
                assertEquals(UserTermsAction.ACCEPTED, saved.getAction());
                assertNotNull(saved.getActionAt());
        }

        @Test
        void revogarTermos_deveFalharQuandoListaForNulaOuVazia() {
                AppUserEntity user = new AppUserEntity();

                assertThrows(NenhumTermoPassadoException.class,
                                () -> termsUserService.revogarTermos(null, user));

                assertThrows(NenhumTermoPassadoException.class,
                                () -> termsUserService.revogarTermos(List.of(), user));

                verifyNoInteractions(termsRepository);
                verifyNoInteractions(userTermsRepository);
        }

        @Test
        void revogarTermos_deveFalharQuandoTermoNaoExistir() {
                UUID termoId = UUID.randomUUID();

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of());

                assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.revogarTermos(List.of(termoId), new AppUserEntity()));

                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void revogarTermos_deveFalharQuandoTermoNaoForVigente() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity optionalType = termType(UUID.randomUUID(), "MARKETING_COMMUNICATION", false);
                TermsEntity optionalTerm = term(UUID.randomUUID(), optionalType, 1, "conteudo", false);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(optionalTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of());

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(optionalTerm));

                TermoNaoEncontradoException exception = assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.revogarTermos(List.of(optionalTerm.getId()), user));

                assertEquals("Termo enviado nao é vigente",
                                exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void revogarTermos_deveFalharQuandoTermoForObrigatorio() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(requiredTerm));

                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> termsUserService.revogarTermos(List.of(requiredTerm.getId()), user));

                assertEquals(
                                "Não é permitido recusar/revogar termo obrigatório: " + requiredType.getName(),
                                exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void revogarTermos_deveSalvarQuandoTermoForOpcionalEVigente() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity optionalType = termType(UUID.randomUUID(), "MARKETING_COMMUNICATION", false);
                TermsEntity optionalTerm = term(UUID.randomUUID(), optionalType, 1, "conteudo", false);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(optionalTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(optionalTerm));

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(optionalTerm));

                termsUserService.revogarTermos(List.of(optionalTerm.getId()), user);

                ArgumentCaptor<UserTermsEntity> captor = ArgumentCaptor.forClass(UserTermsEntity.class);
                verify(userTermsRepository, times(1)).save(captor.capture());

                UserTermsEntity saved = captor.getValue();
                assertEquals(UserTermsAction.REVOKED, saved.getAction());
                assertEquals(optionalTerm, saved.getTerms());
                assertEquals(user, saved.getUser());
        }

        @Test
        void registrarCienciaTermos_deveFalharQuandoListaForNulaOuVazia() {
                AppUserEntity user = new AppUserEntity();

                assertThrows(NenhumTermoPassadoException.class,
                                () -> termsUserService.registrarCienciaTermos(null, user));

                assertThrows(NenhumTermoPassadoException.class,
                                () -> termsUserService.registrarCienciaTermos(List.of(), user));

                verifyNoInteractions(termsRepository);
                verifyNoInteractions(userTermsRepository);
        }

        @Test
        void registrarCienciaTermos_deveFalharQuandoTermoNaoExistir() {
                UUID termoId = UUID.randomUUID();

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of());

                assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.registrarCienciaTermos(List.of(termoId), new AppUserEntity()));

                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void registrarCienciaTermos_deveFalharQuandoTermoNaoForVigente() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity optionalType = termType(UUID.randomUUID(), "MARKETING_COMMUNICATION", false);
                TermsEntity optionalTerm = term(UUID.randomUUID(), optionalType, 1, "conteudo", false);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(optionalTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of());
                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(optionalTerm));

                TermoNaoEncontradoException exception = assertThrows(
                                TermoNaoEncontradoException.class,
                                () -> termsUserService.registrarCienciaTermos(List.of(optionalTerm.getId()), user));

                assertEquals("Termo enviado nao é vigente",
                                exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void registrarCienciaTermos_deveFalharQuandoTermoForObrigatorio() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(requiredTerm));

                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> termsUserService.registrarCienciaTermos(List.of(requiredTerm.getId()), user));

                assertEquals(
                                "Termo obrigatorio deve ser aceito: " + requiredType.getName(),
                                exception.getMessage());
                verify(userTermsRepository, never()).save(any());
        }

        @Test
        void registrarCienciaTermos_deveSalvarQuandoTermoForOpcionalEVigente() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity optionalType = termType(UUID.randomUUID(), "MARKETING_COMMUNICATION", false);
                TermsEntity optionalTerm = term(UUID.randomUUID(), optionalType, 1, "conteudo", false);

                when(termsRepository.findByTermTypeNames(any()))
                                .thenReturn(List.of(optionalTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(optionalTerm));

                when(termsRepository.findAllById(any()))
                                .thenReturn(List.of(optionalTerm));

                termsUserService.registrarCienciaTermos(List.of(optionalTerm.getId()), user);

                ArgumentCaptor<UserTermsEntity> captor = ArgumentCaptor.forClass(UserTermsEntity.class);
                verify(userTermsRepository, times(1)).save(captor.capture());

                UserTermsEntity saved = captor.getValue();
                assertEquals(UserTermsAction.ACKNOWLEDGED, saved.getAction());
                assertEquals(optionalTerm, saved.getTerms());
                assertEquals(user, saved.getUser());
        }

        @Test
        void checkRequiredTerms_deveRetornarTrueQuandoTodosObrigatoriosForemEnviados() {
                AppUserEntity user = null;

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                boolean result = termsUserService.checkRequiredTerms(
                                List.of(requiredTerm.getId()),
                                user,
                                LocalDateTime.now());

                assertTrue(result);
        }

        @Test
        void checkRequiredTerms_deveRetornarFalseQuandoFaltarObrigatorio() {
                AppUserEntity user = null;

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                boolean result = termsUserService.checkRequiredTerms(
                                List.of(),
                                user,
                                LocalDateTime.now());

                assertFalse(result);
        }

        @Test
        void checkRequiredTerms_deveConsiderarTermosJaAceitosPeloUsuario() {
                AppUserEntity user = new AppUserEntity();

                TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

                when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(requiredTerm));

                UserTermsEntity acceptedHistory = userTerm(
                                user,
                                requiredTerm,
                                UserTermsAction.ACCEPTED,
                                LocalDateTime.now().minusHours(1));

                when(userTermsRepository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                                .thenReturn(List.of(acceptedHistory));

                boolean result = termsUserService.checkRequiredTerms(
                                List.of(),
                                user,
                                LocalDateTime.now());

                assertTrue(result);
        }

        @Test
        void findActiveUserTermsAtTime_deveRetornarSomenteUltimaAceitacaoPorTipo() {
                AppUserEntity user = new AppUserEntity();
                LocalDateTime referenceTime = LocalDateTime.now();

                TermTypeEntity tipoA = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity aV1 = term(UUID.randomUUID(), tipoA, 1, "a1", true);
                TermsEntity aV2 = term(UUID.randomUUID(), tipoA, 2, "a2", true);

                TermTypeEntity tipoB = termType(UUID.randomUUID(), "PRIVACY_POLICY", true);
                TermsEntity bV1 = term(UUID.randomUUID(), tipoB, 1, "b1", true);

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(aV1, aV2, bV1));

                when(userTermsRepository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                                .thenReturn(List.of(
                                                userTerm(user, aV1, UserTermsAction.ACCEPTED,
                                                                referenceTime.minusHours(3)),
                                                userTerm(user, aV2, UserTermsAction.ACCEPTED,
                                                                referenceTime.minusHours(1)),
                                                userTerm(user, bV1, UserTermsAction.REVOKED,
                                                                referenceTime.minusMinutes(30))));

                List<UserTermsEntity> result = termsUserService.findAcceptedUserTermsAtTime(user, referenceTime);

                assertEquals(2, result.size());
                assertEquals(UserTermsAction.ACCEPTED, result.get(0).getAction());
        }

        @Test
        void findAcceptedUserTermsAtTime_deveIgnorarTermosNaoVigentes() {
                AppUserEntity user = new AppUserEntity();
                LocalDateTime referenceTime = LocalDateTime.now();

                TermTypeEntity tipoA = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
                TermsEntity activeTerm = term(UUID.randomUUID(), tipoA, 1, "a1", true);

                TermTypeEntity tipoB = termType(UUID.randomUUID(), "PRIVACY_POLICY", true);
                TermsEntity inactiveTerm = term(UUID.randomUUID(), tipoB, 1, "b1", true);

                when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                                .thenReturn(List.of(activeTerm));

                when(userTermsRepository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                                .thenReturn(List.of(
                                                userTerm(user, activeTerm, UserTermsAction.ACCEPTED,
                                                                referenceTime.minusHours(1)),
                                                userTerm(user, inactiveTerm, UserTermsAction.ACCEPTED,
                                                                referenceTime.minusHours(1))));

                List<UserTermsEntity> result = termsUserService.findAcceptedUserTermsAtTime(user, referenceTime);

                assertEquals(1, result.size());
                assertEquals(activeTerm.getId(), result.get(0).getTerms().getId());
        }

        private TermTypeEntity termType(UUID id, String name, boolean required) {
                TermTypeEntity type = new TermTypeEntity();
                type.setId(id);
                type.setName(TermTypeName.valueOf(name));
                type.setIsRequired(required);
                return type;
        }

        private TermsEntity term(
                        UUID id,
                        TermTypeEntity type,
                        int clause,
                        String content,
                        boolean active) {
                TermsEntity term = new TermsEntity();
                term.setId(id);
                term.setTermType(type);
                term.setClause(clause);
                term.setContent(content);
                term.setEffectivityStartAt(LocalDateTime.now().minusDays(1));
                return term;
        }

        private UserTermsEntity userTerm(
                        AppUserEntity user,
                        TermsEntity term,
                        UserTermsAction action,
                        LocalDateTime actionAt) {
                UserTermsEntity userTerm = new UserTermsEntity();
                userTerm.setUser(user);
                userTerm.setTerms(term);
                userTerm.setAction(action);
                userTerm.setActionAt(actionAt);
                return userTerm;
        }
}