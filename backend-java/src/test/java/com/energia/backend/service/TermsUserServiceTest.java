package com.energia.backend.service;

import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.exception.NenhumTermoPassadoException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.UserTermsAction;
import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRepository;
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
    private UserTermsRepository userTermsRespository;
    private TermsUserService service;

    @BeforeEach
    void setUp() {
        termsRepository = mock(TermsRepository.class);
        userTermsRespository = mock(UserTermsRepository.class);
        service = new TermsUserService(termsRepository, userTermsRespository);
    }

    @Test
    void aprovarTermos_deveFalharQuandoListaForNulaOuVazia() {
        AppUserEntity user = new AppUserEntity();

        assertThrows(NenhumTermoPassadoException.class,
                () -> service.aprovarTermos(null, user));

        assertThrows(NenhumTermoPassadoException.class,
                () -> service.aprovarTermos(List.of(), user));

        verifyNoInteractions(termsRepository);
        verifyNoInteractions(userTermsRespository);
    }

    @Test
    void aprovarTermos_deveFalharQuandoAlgumTermoNaoExistir() {
        String termoName = "Termo1";

        when(termsRepository.findByTermTypeNames(any()))
                .thenReturn(List.of());

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.aprovarTermos(List.of(termoName), new AppUserEntity())
        );

        assertTrue(exception.getMessage().startsWith("Um ou mais termos informados nao existem."));
        verify(userTermsRespository, never()).save(any());
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

        when(userTermsRespository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                .thenReturn(List.of());

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.aprovarTermos(List.of(requiredTerm.getTermType().getName(), notActiveTerm.getTermType().getName()), user)
        );

        assertEquals("Termo enviado nao é vigente: " + notActiveTerm.getTermType().getName(), exception.getMessage());
        verify(userTermsRespository, never()).save(any());
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
                true
        );
    
        when(termsRepository.findByTermTypeNames(any()))
                .thenReturn(List.of(outroTermo));
    
        // existe um termo obrigatório vigente
        when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                .thenReturn(List.of(requiredTerm));
    
        when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                .thenReturn(List.of(requiredTerm, outroTermo));
    
        when(userTermsRespository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                .thenReturn(List.of());
    
        DocumentosObrigatoriosNaoConfiguradosException exception = assertThrows(
                DocumentosObrigatoriosNaoConfiguradosException.class,
                () -> service.aprovarTermos(List.of(outroTermo.getTermType().getName()), user)
        );
    
        assertEquals("Termo obrigatorio vigente nao aceito", exception.getMessage());
        verify(userTermsRespository, never()).save(any());
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

        when(userTermsRespository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.aprovarTermos(List.of(requiredTerm.getTermType().getName()), user);

        ArgumentCaptor<UserTermsEntity> captor = ArgumentCaptor.forClass(UserTermsEntity.class);
        verify(userTermsRespository, times(1)).save(captor.capture());

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
                () -> service.revogarTermos(null, user));

        assertThrows(NenhumTermoPassadoException.class,
                () -> service.revogarTermos(List.of(), user));

        verifyNoInteractions(termsRepository);
        verifyNoInteractions(userTermsRespository);
    }

    @Test
    void revogarTermos_deveFalharQuandoTermoNaoExistir() {
        String termoName = "Termo1";

        when(termsRepository.findByTermTypeNames(any()))
                .thenReturn(List.of());

        assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.revogarTermos(List.of(termoName), new AppUserEntity())
        );

        verify(userTermsRespository, never()).save(any());
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

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.revogarTermos(List.of(optionalTerm.getTermType().getName()), user)
        );

        assertEquals("Termo enviado nao é vigente: " + optionalTerm.getTermType().getName(), exception.getMessage());
        verify(userTermsRespository, never()).save(any());
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

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.revogarTermos(List.of(requiredTerm.getTermType().getName()), user)
        );

        assertEquals(
                "Nao é permitido revogar termo obrigatorio: " + requiredType.getName(),
                exception.getMessage()
        );
        verify(userTermsRespository, never()).save(any());
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

        service.revogarTermos(List.of(optionalTerm.getTermType().getName()), user);

        ArgumentCaptor<UserTermsEntity> captor = ArgumentCaptor.forClass(UserTermsEntity.class);
        verify(userTermsRespository, times(1)).save(captor.capture());

        UserTermsEntity saved = captor.getValue();
        assertEquals(UserTermsAction.REVOKED, saved.getAction());
        assertEquals(optionalTerm, saved.getTerms());
        assertEquals(user, saved.getUser());
    }

    @Test
    void registrarCienciaTermos_deveFalharQuandoListaForNulaOuVazia() {
        AppUserEntity user = new AppUserEntity();

        assertThrows(NenhumTermoPassadoException.class,
                () -> service.registrarCienciaTermos(null, user));

        assertThrows(NenhumTermoPassadoException.class,
                () -> service.registrarCienciaTermos(List.of(), user));

        verifyNoInteractions(termsRepository);
        verifyNoInteractions(userTermsRespository);
    }

    @Test
    void registrarCienciaTermos_deveFalharQuandoTermoNaoExistir() {
        String termoName = "Termo1";

        when(termsRepository.findByTermTypeNames(any()))
                .thenReturn(List.of());

        assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.registrarCienciaTermos(List.of(termoName), new AppUserEntity())
        );

        verify(userTermsRespository, never()).save(any());
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

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.registrarCienciaTermos(List.of(optionalTerm.getTermType().getName()), user)
        );

        assertEquals("Termo enviado nao é vigente: " + optionalTerm.getTermType().getName(), exception.getMessage());
        verify(userTermsRespository, never()).save(any());
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

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.registrarCienciaTermos(List.of(requiredTerm.getTermType().getName()), user)
        );

        assertEquals(
                "Termo obrigatorio deve ser aceito: " + requiredType.getName(),
                exception.getMessage()
        );
        verify(userTermsRespository, never()).save(any());
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

        service.registrarCienciaTermos(List.of(optionalTerm.getTermType().getName()), user);

        ArgumentCaptor<UserTermsEntity> captor = ArgumentCaptor.forClass(UserTermsEntity.class);
        verify(userTermsRespository, times(1)).save(captor.capture());

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

        boolean result = service.checkRequiredTerms(
                List.of(requiredTerm.getTermType().getName()),
                user,
                LocalDateTime.now()
        );

        assertTrue(result);
    }

    @Test
    void checkRequiredTerms_deveRetornarFalseQuandoFaltarObrigatorio() {
        AppUserEntity user = null;

        TermTypeEntity requiredType = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
        TermsEntity requiredTerm = term(UUID.randomUUID(), requiredType, 1, "conteudo", true);

        when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                .thenReturn(List.of(requiredTerm));

        boolean result = service.checkRequiredTerms(
                List.of(),
                user,
                LocalDateTime.now()
        );

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
                LocalDateTime.now().minusHours(1)
        );

        when(userTermsRespository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                .thenReturn(List.of(acceptedHistory));

        boolean result = service.checkRequiredTerms(
                List.of(),
                user,
                LocalDateTime.now()
        );

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

        when(userTermsRespository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        userTerm(user, aV1, UserTermsAction.ACCEPTED, referenceTime.minusHours(3)),
                        userTerm(user, aV2, UserTermsAction.ACCEPTED, referenceTime.minusHours(1)),
                        userTerm(user, bV1, UserTermsAction.REVOKED, referenceTime.minusMinutes(30))
                ));

        List<UserTermsEntity> result = service.findActiveUserTermsAtTime(user, referenceTime);

        assertEquals(1, result.size());
        assertEquals(aV2.getId(), result.get(0).getTerms().getId());
        assertEquals(UserTermsAction.ACCEPTED, result.get(0).getAction());
    }

    @Test
    void findActiveUserTermsAtTime_deveIgnorarTermosNaoVigentes() {
        AppUserEntity user = new AppUserEntity();
        LocalDateTime referenceTime = LocalDateTime.now();

        TermTypeEntity tipoA = termType(UUID.randomUUID(), "TERMS_OF_USE", true);
        TermsEntity activeTerm = term(UUID.randomUUID(), tipoA, 1, "a1", true);

        TermTypeEntity tipoB = termType(UUID.randomUUID(), "PRIVACY_POLICY", true);
        TermsEntity inactiveTerm = term(UUID.randomUUID(), tipoB, 1, "b1", true);

        when(termsRepository.findActiveByReferenceTime(any(LocalDateTime.class)))
                .thenReturn(List.of(activeTerm));

        when(userTermsRespository.findByUserAndActionAtLessThanEqual(eq(user), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        userTerm(user, activeTerm, UserTermsAction.ACCEPTED, referenceTime.minusHours(1)),
                        userTerm(user, inactiveTerm, UserTermsAction.ACCEPTED, referenceTime.minusHours(1))
                ));

        List<UserTermsEntity> result = service.findActiveUserTermsAtTime(user, referenceTime);

        assertEquals(1, result.size());
        assertEquals(activeTerm.getId(), result.get(0).getTerms().getId());
    }

    private TermTypeEntity termType(UUID id, String name, boolean required) {
        TermTypeEntity type = new TermTypeEntity();
        type.setId(id);
        type.setName(name);
        type.setIsRequired(required);
        return type;
    }

    private TermsEntity term(
            UUID id,
            TermTypeEntity type,
            int version,
            String content,
            boolean active
    ) {
        TermsEntity term = new TermsEntity();
        term.setId(id);
        term.setTermType(type);
        term.setVersion(version);
        term.setContent(content);
        term.setIsActive(active);
        term.setEffectivityStartAt(LocalDateTime.now().minusDays(1));
        return term;
    }

    private UserTermsEntity userTerm(
            AppUserEntity user,
            TermsEntity term,
            UserTermsAction action,
            LocalDateTime actionAt
    ) {
        UserTermsEntity userTerm = new UserTermsEntity();
        userTerm.setUser(user);
        userTerm.setTerms(term);
        userTerm.setAction(action);
        userTerm.setActionAt(actionAt);
        return userTerm;
    }
}