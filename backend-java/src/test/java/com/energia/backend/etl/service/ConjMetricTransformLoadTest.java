package com.energia.backend.etl.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.energia.backend.model.aneel.*;
import com.energia.backend.repository.aneel.*;
import com.energia.backend.etl.exception.DuplicatesDetectedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConjMetricTransformLoadTest {

    private ConjuntoRepository conjuntoRepository;
    private MetricasRepository metricasRepository;
    private ColetaDadosRepository coletaDadosRepository;
    private DistribuidoraRepository distribuidoraRepository;
    private SigIndicadorRepository sigIndicadorRepository;
    private ConjMetricTransformLoad service;

    @BeforeEach
    void setup() {
        conjuntoRepository = mock(ConjuntoRepository.class);
        metricasRepository = mock(MetricasRepository.class);
        coletaDadosRepository = mock(ColetaDadosRepository.class);
        distribuidoraRepository = mock(DistribuidoraRepository.class);
        sigIndicadorRepository = mock(SigIndicadorRepository.class);

        service = new ConjMetricTransformLoad(
                conjuntoRepository,
                metricasRepository,
                coletaDadosRepository,
                distribuidoraRepository,
                sigIndicadorRepository
        );
    }

    @Test
    void deveProcessarJsonComDados() throws com.fasterxml.jackson.core.JsonProcessingException {
        String json = createValidJson(1);

        Distribuidora distribuidora = createMockDistribuidora();
        Conjunto conjunto = createMockConjunto(1L, distribuidora);
        SigIndicador indicador = createMockSigIndicador();

        when(conjuntoRepository.findByIdeConjUndConsumidoras(1L)).thenReturn(Optional.empty());
        when(distribuidoraRepository.findByNumCnpj("97578090000134")).thenReturn(Optional.of(distribuidora));
        when(conjuntoRepository.save(any(Conjunto.class))).thenReturn(conjunto);
        when(sigIndicadorRepository.findByIndicadorType(IndicadorType.DEC)).thenReturn(Optional.of(indicador));
        when(metricasRepository.findByConjuntoAndSigIndicadorAndNumPeriodoIndiceAndAnoIndice(
                conjunto, indicador, 1L, 2024L
        )).thenReturn(Optional.empty());

        service.processarJson(json);

        verify(conjuntoRepository).save(any(Conjunto.class));
        verify(metricasRepository).save(any(Metricas.class));
        verify(coletaDadosRepository, atLeastOnce()).save(any(ColetaDados.class));
    }

    @Test
    void deveLancarExcecaoQuandoEncontraDuplicata() {
        String json = createValidJson(1);

        Distribuidora distribuidora = createMockDistribuidora();
        Conjunto conjunto = createMockConjunto(1L, distribuidora);
        SigIndicador indicador = createMockSigIndicador();
        Metricas metricaExistente = createMockMetrica(conjunto, indicador);

        when(conjuntoRepository.findByIdeConjUndConsumidoras(1L)).thenReturn(Optional.of(conjunto));
        when(sigIndicadorRepository.findByIndicadorType(IndicadorType.DEC)).thenReturn(Optional.of(indicador));
        when(metricasRepository.findByConjuntoAndSigIndicadorAndNumPeriodoIndiceAndAnoIndice(
                conjunto, indicador, 1L, 2024L
        )).thenReturn(Optional.of(metricaExistente));

        DuplicatesDetectedException exception = assertThrows(DuplicatesDetectedException.class, () -> {
            service.processarJson(json);
        });

        assertEquals(1, exception.getCount());
    }

    @Test
    void deveProcessarMultiploRegistrosComDuplicatas() throws com.fasterxml.jackson.core.JsonProcessingException {
        String json = createJsonComMultiplosRegistros();

        Distribuidora distribuidora = createMockDistribuidora();
        Conjunto conjunto = createMockConjunto(1L, distribuidora);
        SigIndicador indicador = createMockSigIndicador();
        Metricas metricaExistente = createMockMetrica(conjunto, indicador);

        when(conjuntoRepository.findByIdeConjUndConsumidoras(1L)).thenReturn(Optional.of(conjunto));
        when(sigIndicadorRepository.findByIndicadorType(IndicadorType.DEC)).thenReturn(Optional.of(indicador));
        when(metricasRepository.findByConjuntoAndSigIndicadorAndNumPeriodoIndiceAndAnoIndice(
                any(), any(), anyLong(), anyLong()
        )).thenReturn(Optional.of(metricaExistente));

        DuplicatesDetectedException exception = assertThrows(DuplicatesDetectedException.class, () -> {
            service.processarJson(json);
        });

        assertEquals(2, exception.getCount());
    }

    @Test
    void deveCriarColetaDadosAoNovoConjunto() throws com.fasterxml.jackson.core.JsonProcessingException {
        String json = createValidJson(1);

        Distribuidora distribuidora = createMockDistribuidora();
        Conjunto conjunto = createMockConjunto(1L, distribuidora);
        SigIndicador indicador = createMockSigIndicador();

        when(conjuntoRepository.findByIdeConjUndConsumidoras(1L)).thenReturn(Optional.empty());
        when(distribuidoraRepository.findByNumCnpj("97578090000134")).thenReturn(Optional.of(distribuidora));
        when(conjuntoRepository.save(any(Conjunto.class))).thenReturn(conjunto);
        when(sigIndicadorRepository.findByIndicadorType(IndicadorType.DEC)).thenReturn(Optional.of(indicador));
        when(metricasRepository.findByConjuntoAndSigIndicadorAndNumPeriodoIndiceAndAnoIndice(
                conjunto, indicador, 1L, 2024L
        )).thenReturn(Optional.empty());

        service.processarJson(json);

        verify(coletaDadosRepository, atLeastOnce()).save(argThat(coleta ->
                coleta.getDataKey() == DataKey.CONJUNTO || coleta.getDataKey() == DataKey.METRICAS
        ));
    }

    private String createValidJson(int quantidadeRegistros) {
        StringBuilder json = new StringBuilder();
        json.append("{\"result\":{\"records\":[");

        for (int i = 0; i < quantidadeRegistros; i++) {
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"IdeConjUndConsumidoras\":1,");
            json.append("\"DscConjUndConsumidoras\":\"Conjunto Test\",");
            json.append("\"NumCNPJ\":\"97.578.090/0001-34\",");
            json.append("\"SigIndicador\":\"DEC\",");
            json.append("\"NumPeriodoIndice\":1,");
            json.append("\"AnoIndice\":2024,");
            json.append("\"VlrIndiceEnviado\":\"10,50\",");
            json.append("\"DatGeracaoConjuntoDados\":\"2024-01-01\"");
            json.append("}");
        }

        json.append("]}}");
        return json.toString();
    }

    private String createJsonComMultiplosRegistros() {
        return "{\"result\":{\"records\":[" +
                "{" +
                "\"IdeConjUndConsumidoras\":1," +
                "\"DscConjUndConsumidoras\":\"Conjunto 1\"," +
                "\"NumCNPJ\":\"97.578.090/0001-34\"," +
                "\"SigIndicador\":\"DEC\"," +
                "\"NumPeriodoIndice\":1," +
                "\"AnoIndice\":2024," +
                "\"VlrIndiceEnviado\":\"10,50\"," +
                "\"DatGeracaoConjuntoDados\":\"2024-01-01\"" +
                "}," +
                "{" +
                "\"IdeConjUndConsumidoras\":1," +
                "\"DscConjUndConsumidoras\":\"Conjunto 1\"," +
                "\"NumCNPJ\":\"97.578.090/0001-34\"," +
                "\"SigIndicador\":\"DEC\"," +
                "\"NumPeriodoIndice\":1," +
                "\"AnoIndice\":2024," +
                "\"VlrIndiceEnviado\":\"10,50\"," +
                "\"DatGeracaoConjuntoDados\":\"2024-01-01\"" +
                "}" +
                "]}}";
    }

    private Distribuidora createMockDistribuidora() {
        Distribuidora dist = new Distribuidora();
        dist.setId(1L);
        dist.setNumCnpj("97578090000134");
        return dist;
    }

    private Conjunto createMockConjunto(Long id, Distribuidora distribuidora) {
        Conjunto conjunto = new Conjunto();
        conjunto.setId(id);
        conjunto.setIdeConjUndConsumidoras(1L);
        conjunto.setDscConjUndConsumidoras("Conjunto Test");
        conjunto.setDistribuidora(distribuidora);
        return conjunto;
    }

    private SigIndicador createMockSigIndicador() {
        SigIndicador indicador = new SigIndicador();
        indicador.setId(1L);
        indicador.setIndicadorType(IndicadorType.DEC);
        return indicador;
    }

    private Metricas createMockMetrica(Conjunto conjunto, SigIndicador indicador) {
        Metricas metrica = new Metricas();
        metrica.setId(1L);
        metrica.setConjunto(conjunto);
        metrica.setSigIndicador(indicador);
        metrica.setNumPeriodoIndice(1L);
        metrica.setAnoIndice(2024L);
        return metrica;
    }
}
