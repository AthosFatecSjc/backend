package com.energia.backend.etl.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.energia.backend.model.aneel.ColetaDados;
import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.Distribuidora;
import com.energia.backend.model.aneel.IndicadorType;
import com.energia.backend.model.aneel.Metricas;
import com.energia.backend.model.aneel.SigIndicador;
import com.energia.backend.model.aneel.DataKey;
import com.energia.backend.etl.Utils;
import com.energia.backend.etl.exception.DuplicatesDetectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.energia.backend.repository.aneel.ColetaDadosRepository;
import com.energia.backend.repository.aneel.ConjuntoRepository;
import com.energia.backend.repository.aneel.DistribuidoraRepository;
import com.energia.backend.repository.aneel.IndicatorType;
import com.energia.backend.repository.aneel.MetricasRepository;
import com.energia.backend.repository.aneel.SigIndicadorRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConjMetricTransformLoad {

    private final ConjuntoRepository conjuntoRepository;
    private final MetricasRepository metricasRepository;
    private final ColetaDadosRepository coletaDadosRepository;
    private final DistribuidoraRepository distribuidoraRepository;
    private final SigIndicadorRepository sigIndicadorRepository;

    @Transactional(dontRollbackOn = DuplicatesDetectedException.class)
    public void processarJson(String json) throws com.fasterxml.jackson.core.JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> response = mapper.readValue(json, Map.class);
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        List<Map<String, Object>> registros = result == null
            ? List.of()
            : (List<Map<String, Object>>) result.getOrDefault("records", List.of());

        if (registros.isEmpty()) {
            throw new IllegalStateException("Erro na extração ANEEL: resposta de métricas sem registros");
        }

        List<String> errosDuplicatas = new ArrayList<>();
        int linhasValidas = 0;

        for (Map<String, Object> row : registros) {

            Long ideConjUndConsumidoras = Utils.toLong(asString(row.get("IdeConjUndConsumidoras")));
            String dscConjUndConsumidoras = Utils.cleanNullable(asString(row.get("DscConjUndConsumidoras")));
            String numCnpj = normalizeCnpj(asString(row.get("NumCNPJ")));

            String sigIndicador = Utils.cleanNullable(asString(row.get("SigIndicador")));
            Long numPeriodoIndice = Utils.toLong(asString(row.get("NumPeriodoIndice")));
            Long anoIndice = Utils.toLong(asString(row.get("AnoIndice")));
            Double vlrIndiceEnviado = Utils.toDoubleBrNullable(asString(row.get("VlrIndiceEnviado")));
            LocalDate dataGeracaoConjDados = Utils.toDateNullable(asString(row.get("DatGeracaoConjuntoDados")));

            if (ideConjUndConsumidoras == null || numCnpj == null || sigIndicador == null
                    || numPeriodoIndice == null || anoIndice == null) {
                log.warn("Linha de métricas ignorada por campos obrigatórios inválidos: {}", row);
                continue;
            }

            LocalDate dataColeta = LocalDate.now();

            Conjunto conjunto = conjuntoRepository
                .findByIdeConjUndConsumidoras(ideConjUndConsumidoras)
                .orElse(null);

            boolean created = false;

            if (conjunto == null) {
                Distribuidora dist = distribuidoraRepository
                    .findByNumCnpj(numCnpj)
                    .orElse(null);
                if (dist == null) {
                    log.warn("Distribuidora não encontrada para CNPJ {}. Linha ignorada: {}", numCnpj, row);
                    continue;
                }

                conjunto = new Conjunto();
                conjunto.setIdeConjUndConsumidoras(ideConjUndConsumidoras);
                conjunto.setDscConjUndConsumidoras(dscConjUndConsumidoras);
                conjunto.setDistribuidora(dist);

                conjunto = conjuntoRepository.save(conjunto);
                created = true;
            }

            if (created) {
                ColetaDados coleta = new ColetaDados();
                coleta.setDataColeta(dataColeta);
                coleta.setDataGeracao(dataGeracaoConjDados);
                coleta.setDataKey(DataKey.CONJUNTO);
                coleta.setIdData(conjunto.getId());
                coleta.setLink("https://dadosabertos.aneel.gov.br/dataset/indicadores-coletivos-de-continuidade-dec-e-fec");

                coletaDadosRepository.save(coleta);
            }

            IndicadorType tipo;
            try {
                tipo = IndicadorType.valueOf(sigIndicador.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                log.warn("SigIndicador inválido em métricas: {}. Linha ignorada: {}", sigIndicador, row);
                continue;
            }

            SigIndicador indicador = sigIndicadorRepository
                .findByIndicadorType(tipo)
                .orElse(null);
            if (indicador == null) {
                log.warn("Indicador não encontrado para tipo {}. Linha ignorada: {}", tipo, row);
                continue;
            }

            Optional<Metricas> opt = metricasRepository
                .findByConjuntoAndSigIndicadorAndNumPeriodoIndiceAndAnoIndice(
                    conjunto, indicador, numPeriodoIndice, anoIndice
                );

            if (!opt.isPresent()) {
                Metricas metricaNova = new Metricas();
                metricaNova.setConjunto(conjunto);
                metricaNova.setSigIndicador(indicador);
                metricaNova.setNumPeriodoIndice(numPeriodoIndice);
                metricaNova.setAnoIndice(anoIndice);
                metricaNova.setDataGeracaoConjDados(dataGeracaoConjDados);
                metricaNova.setVlrIndiceEnviado(vlrIndiceEnviado);

                metricasRepository.save(metricaNova);

                ColetaDados coleta = new ColetaDados();
                coleta.setDataColeta(dataColeta);
                coleta.setDataGeracao(dataGeracaoConjDados);
                coleta.setDataKey(DataKey.METRICAS);
                coleta.setIdData(metricaNova.getId());
                coleta.setLink("https://dadosabertos.aneel.gov.br/dataset/indicadores-coletivos-de-continuidade-dec-e-fec");

                coletaDadosRepository.save(coleta);
                linhasValidas++;

            } else {
                String msgDuplicata = String.format(
                    "Métrica duplicada pulada - Conjunto %d, Indicador %s, Período %d, Ano %d",
                    conjunto.getId(),
                    indicador.getIndicadorType(),
                    numPeriodoIndice,
                    anoIndice
                );
                errosDuplicatas.add(msgDuplicata);
            }
        }

        if (linhasValidas == 0 && errosDuplicatas.isEmpty()) {
            throw new IllegalStateException("Erro na extração ANEEL: nenhum registro válido de métricas foi processado");
        }

        if (!errosDuplicatas.isEmpty()) {
            throw new DuplicatesDetectedException(
                errosDuplicatas.size(),
                String.join("; ", errosDuplicatas)
            );
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String normalizeCnpj(String raw) {
        String cleaned = Utils.cleanNullable(raw);
        if (cleaned == null) {
            return null;
        }
        return cleaned.replaceAll("[^0-9]", "");
    }
}