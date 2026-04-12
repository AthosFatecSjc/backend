package com.energia.backend.service;

import com.energia.backend.dto.TermoRequest;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermTypeRepository;
import com.energia.backend.repository.TermsJpaRepository;
import com.energia.backend.repository.TermsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TermsServiceCadastroTest {

    private TermsRepository termsRepository;
    private TermsJpaRepository termsJpaRepository;
    private TermTypeRepository termTypeRepository;
    private TermsService service;

    @BeforeEach
    void setUp() {
        termsRepository = mock(TermsRepository.class);
        termsJpaRepository = mock(TermsJpaRepository.class);
        termTypeRepository = mock(TermTypeRepository.class);

        service = new TermsService(
            termsRepository,
            termsJpaRepository,
            termTypeRepository
        );
    }

    @Test
    void deveCriarPrimeiraVersaoQuandoNaoExisteAnterior() {

        TermoRequest request = new TermoRequest();
        request.setTermTypeName("TERMS_OF_USE");
        request.setContent("conteudo");

        TermTypeEntity type = new TermTypeEntity();
        type.setId(UUID.randomUUID());
        type.setName("TERMS_OF_USE");

        when(termTypeRepository.findByNameIgnoreCase("TERMS_OF_USE"))
                .thenReturn(Optional.of(type));

        when(termsJpaRepository.findMaxVersionByTermType(type.getId()))
                .thenReturn(null);

        when(termsRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TermsEntity result = service.cadastrarNovoTermo(request);

        assertEquals(1, result.getVersion());
        assertTrue(result.getIsActive());
        assertNotNull(result.getEffectivityStartAt());

        verify(termsJpaRepository).deactivateByType(type.getId());
        verify(termsRepository).save(any());
    }

    @Test
    void deveIncrementarVersaoQuandoJaExisteVersaoAnterior() {

        TermoRequest request = new TermoRequest();
        request.setTermTypeName("PRIVACY_POLICY");
        request.setContent("conteudo");

        TermTypeEntity type = new TermTypeEntity();
        type.setId(UUID.randomUUID());

        when(termTypeRepository.findByNameIgnoreCase("PRIVACY_POLICY"))
                .thenReturn(Optional.of(type));

        when(termsJpaRepository.findMaxVersionByTermType(type.getId()))
                .thenReturn(3);

        when(termsRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TermsEntity result = service.cadastrarNovoTermo(request);

        assertEquals(4, result.getVersion());
    }

    @Test
    void deveFalharQuandoTipoNaoExiste() {

        TermoRequest request = new TermoRequest();
        request.setTermTypeName("NAO_EXISTE");

        when(termTypeRepository.findByNameIgnoreCase("NAO_EXISTE"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.cadastrarNovoTermo(request)
        );

        assertEquals(
                "Tipo de termo não encontrado: NAO_EXISTE",
                exception.getMessage()
        );

        verifyNoInteractions(termsRepository);
        verifyNoInteractions(termsJpaRepository);
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

        when(termTypeRepository.findByNameIgnoreCase(any()))
                .thenReturn(Optional.of(type));

        when(termsRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TermsEntity result = service.cadastrarNovoTermo(request);

        assertEquals(inicio, result.getEffectivityStartAt());
    }

    @Test
    void deveUsarDataAtualQuandoEffectivityNaoInformado() {

        TermoRequest request = new TermoRequest();
        request.setTermTypeName("TERMS_OF_USE");
        request.setContent("conteudo");

        TermTypeEntity type = new TermTypeEntity();
        type.setId(UUID.randomUUID());

        when(termTypeRepository.findByNameIgnoreCase(any()))
                .thenReturn(Optional.of(type));

        when(termsRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TermsEntity result = service.cadastrarNovoTermo(request);

        assertNotNull(result.getEffectivityStartAt());
    }
}