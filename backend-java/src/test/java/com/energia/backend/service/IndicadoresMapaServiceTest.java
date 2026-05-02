package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.energia.backend.dto.MapaCalorConjuntoResponse;
import com.energia.backend.dto.MapaCalorResponse;
import com.energia.backend.repository.aneel.ConjuntoRepository;
import com.energia.backend.repository.aneel.projection.MapaCalorConjuntoProjection;
import com.fasterxml.jackson.databind.ObjectMapper;

class IndicadoresMapaServiceTest {

    private IndicadoresMapaService service;

    @BeforeEach
    void setUp() {
        service = new IndicadoresMapaService(repository(), new ObjectMapper());
    }

    @Test
    void deveClassificarMapaCalorPelaMediaAritmeticaDeDecEFecContraMediaDosLimites() {
        MapaCalorResponse response = service.obterMapaCalor(2026L);

        MapaCalorConjuntoResponse conjunto = response.getConjuntos().get(0);

        assertEquals("media-dec-fec", conjunto.getIndicadorPrincipal().getId());
        assertEquals("Media DEC/FEC", conjunto.getIndicadorPrincipal().getLabel());
        assertEquals(5.0, conjunto.getIndicadorPrincipal().getValor());
        assertEquals(10.0, conjunto.getIndicadorPrincipal().getLimite());
        assertEquals("moderado", conjunto.getCriticidade());
    }

    private ConjuntoRepository repository() {
        return (ConjuntoRepository) Proxy.newProxyInstance(
                ConjuntoRepository.class.getClassLoader(),
                new Class<?>[] { ConjuntoRepository.class },
                (proxy, method, args) -> {
                    if ("buscarDadosMapaCalor".equals(method.getName())) {
                        return List.of(projection());
                    }

                    if ("listarAnosDisponiveisMapaCalor".equals(method.getName())) {
                        return List.of(2026L);
                    }

                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private MapaCalorConjuntoProjection projection() {
        return new MapaCalorConjuntoProjection() {
            @Override
            public Long getConjuntoId() {
                return 1L;
            }

            @Override
            public Long getIdeConjUndConsumidoras() {
                return 123L;
            }

            @Override
            public String getDscConjUndConsumidoras() {
                return "Conjunto Teste";
            }

            @Override
            public String getRazaoSocial() {
                return "Distribuidora Teste";
            }

            @Override
            public String getUf() {
                return "SP";
            }

            @Override
            public Long getAnoReferencia() {
                return 2026L;
            }

            @Override
            public Long getPeriodoReferencia() {
                return 1L;
            }

            @Override
            public Double getDecValor() {
                return 10.0;
            }

            @Override
            public Double getFecValor() {
                return 0.0;
            }

            @Override
            public Double getDecLim() {
                return 5.0;
            }

            @Override
            public Double getFecLim() {
                return 15.0;
            }

            @Override
            public Double getPerdasNaoTec() {
                return null;
            }

            @Override
            public Double getCustoPerdasNaoTec() {
                return null;
            }

            @Override
            public String getGeometryGeojson() {
                return "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,0]]]}";
            }
        };
    }
}
