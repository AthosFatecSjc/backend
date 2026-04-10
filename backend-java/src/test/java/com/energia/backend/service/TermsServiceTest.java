package com.energia.backend.service;

import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRespository;
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
        UserTermsRespository userTermsRespository = mock(UserTermsRespository.class);
        TermsService service = new TermsService(termsRepository, userTermsRespository);

        TermsEntity marketing = termo("MARKETING_COMMUNICATION", 1, false);
        TermsEntity termosDeUsoVigente = termo("TERMS_OF_USE", 2, true);
        TermsEntity politicaPrivacidadeVigente = termo("PRIVACY_POLICY", 3, true);

        when(termsRepository.findAllWithTypeByIdIn(List.of(marketing.getId()))).thenReturn(List.of(marketing));
        when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                .thenReturn(List.of(termosDeUsoVigente, politicaPrivacidadeVigente));

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.registrarTermosAceitos(List.of(marketing.getId()), new AppUserEntity())
        );

        assertEquals("Termo obrigatorio vigente nao aceito: TERMS_OF_USE", exception.getMessage());
        verify(userTermsRespository, never()).save(any());
    }

    @Test
    void deveRejeitarQuandoRecebeVersaoAntigaDeTermoObrigatorio() {
        TermsRepository termsRepository = mock(TermsRepository.class);
        UserTermsRespository userTermsRespository = mock(UserTermsRespository.class);
        TermsService service = new TermsService(termsRepository, userTermsRespository);

        TermsEntity termosDeUsoAntigo = termo("TERMS_OF_USE", 1, true);
        TermsEntity termosDeUsoVigente = termo("TERMS_OF_USE", 2, true);
        TermsEntity politicaPrivacidadeVigente = termo("PRIVACY_POLICY", 1, true);

        when(termsRepository.findAllWithTypeByIdIn(List.of(termosDeUsoAntigo.getId(), politicaPrivacidadeVigente.getId())))
                .thenReturn(List.of(termosDeUsoAntigo, politicaPrivacidadeVigente));
        when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                .thenReturn(List.of(termosDeUsoVigente, politicaPrivacidadeVigente));

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.registrarTermosAceitos(
                        List.of(termosDeUsoAntigo.getId(), politicaPrivacidadeVigente.getId()),
                        new AppUserEntity())
        );

        assertEquals("Termo obrigatorio vigente nao aceito: TERMS_OF_USE", exception.getMessage());
        verify(userTermsRespository, never()).save(any());
    }

    @Test
    void devePersistirAceitesQuandoRecebeOsTermosObrigatoriosVigentes() {
        TermsRepository termsRepository = mock(TermsRepository.class);
        UserTermsRespository userTermsRespository = mock(UserTermsRespository.class);
        TermsService service = new TermsService(termsRepository, userTermsRespository);

        TermsEntity termosDeUsoVigente = termo("TERMS_OF_USE", 2, true);
        TermsEntity politicaPrivacidadeVigente = termo("PRIVACY_POLICY", 1, true);

        when(termsRepository.findAllWithTypeByIdIn(List.of(termosDeUsoVigente.getId(), politicaPrivacidadeVigente.getId())))
                .thenReturn(List.of(termosDeUsoVigente, politicaPrivacidadeVigente));
        when(termsRepository.findActiveRequiredByReferenceTime(any(LocalDateTime.class)))
                .thenReturn(List.of(termosDeUsoVigente, politicaPrivacidadeVigente));
        when(userTermsRespository.findHistoryByUserId(any(UUID.class))).thenReturn(List.of());

        service.registrarTermosAceitos(
                List.of(termosDeUsoVigente.getId(), politicaPrivacidadeVigente.getId()),
                new AppUserEntity());

        verify(userTermsRespository, times(2)).save(any());
    }

    @Test
    void deveRejeitarQuandoRecebeTermoInexistente() {
        TermsRepository termsRepository = mock(TermsRepository.class);
        UserTermsRespository userTermsRespository = mock(UserTermsRespository.class);
        TermsService service = new TermsService(termsRepository, userTermsRespository);

        UUID termoId = UUID.randomUUID();
        when(termsRepository.findAllWithTypeByIdIn(List.of(termoId))).thenReturn(List.of());

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.registrarTermosAceitos(List.of(termoId), new AppUserEntity())
        );

        assertEquals("Um ou mais termos informados nao existem.", exception.getMessage());
        verify(userTermsRespository, never()).save(any());
    }

    private TermsEntity termo(String typeName, int version, boolean required) {
        TermTypeEntity tipo = new TermTypeEntity();
        tipo.setName(typeName);

        TermsEntity termo = new TermsEntity();
        termo.setId(UUID.randomUUID());
        termo.setTermType(tipo);
        termo.setVersion(version);
        termo.setCreatedAt(LocalDateTime.now());
        termo.setEffectivityStartAt(LocalDateTime.now());
        termo.setContent(typeName);
        termo.setIsRequired(required);
        return termo;
    }
}
