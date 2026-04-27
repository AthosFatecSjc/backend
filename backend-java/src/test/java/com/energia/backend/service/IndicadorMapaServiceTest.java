package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.energia.backend.dto.CriticidadeMapaResponse;
import com.energia.backend.repository.aneel.MetricasRepository;
import com.energia.backend.repository.aneel.projection.CriticidadeMapaProjection;

@ExtendWith(MockitoExtension.class)
class IndicadorMapaServiceTest {

    @Mock
    private MetricasRepository metricasRepository;

    private IndicadorMapaService service;

    @BeforeEach
    void setUp() {
        service = new IndicadorMapaService(metricasRepository);
    }

    @Test
    void deveRetornarFaixasCorretas() {
        when(metricasRepository.buscarCriticidadeParaMapa(anyLong(), anyLong(), any())).thenReturn(List.of(
                projection("Conjunto Verde", 49.99),
                projection("Conjunto Amarelo", 100.0),
                projection("Conjunto Vermelho", 130.18)
        ));

        List<CriticidadeMapaResponse> resposta = service.buscarCriticidade(1L, 2026L, null);

        assertEquals(3, resposta.size());
        assertEquals("VERDE", resposta.get(0).getFaixa());
        assertEquals("AMARELO", resposta.get(1).getFaixa());
        assertEquals("VERMELHO", resposta.get(2).getFaixa());
    }

    @Test
    void deveLancarExcecaoQuandoMesInvalido() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarCriticidade(13L, 2026L, null)
        );

        assertEquals("Parametro 'mes' deve estar entre 1 e 12.", ex.getMessage());
    }

    @Test
    void deveNormalizarFiltroNomeConjunto() {
        when(metricasRepository.buscarCriticidadeParaMapa(anyLong(), anyLong(), any())).thenReturn(List.of());

        service.buscarCriticidade(1L, 2026L, "   ");

        verify(metricasRepository).buscarCriticidadeParaMapa(1L, 2026L, null);
    }

    private CriticidadeMapaProjection projection(String nome, Double indice) {
        return new CriticidadeMapaProjection() {
            @Override
            public String getNomeConjunto() {
                return nome;
            }

            @Override
            public Double getIndiceCriticidadePercentual() {
                return indice;
            }
        };
    }
}
