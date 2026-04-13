package com.energia.backend.etl.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.energia.backend.model.aneel.ColetaDados;
import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.Distribuidora;
import com.energia.backend.model.aneel.IndicadorType;
import com.energia.backend.model.aneel.Metricas;
import com.energia.backend.model.aneel.SigIndicador;
import com.energia.backend.model.aneel.DataKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.energia.backend.repository.aneel.ColetaDadosRepository;
import com.energia.backend.repository.aneel.ConjuntoRepository;
import com.energia.backend.repository.aneel.DistribuidoraRepository;
import com.energia.backend.repository.aneel.IndicatorType;
import com.energia.backend.repository.aneel.MetricasRepository;
import com.energia.backend.repository.aneel.SigIndicadorRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConjMetricTransformLoad {

    private final ConjuntoRepository conjuntoRepository;
    private final MetricasRepository metricasRepository;
    private final ColetaDadosRepository coletaDadosRepository;
    private final DistribuidoraRepository distribuidoraRepository;
    private final SigIndicadorRepository sigIndicadorRepository;

    @Transactional
    public void processarJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
        
            Map<String, Object> response = mapper.readValue(json, Map.class);
        
            Map<String, Object> result = (Map<String, Object>) response.get("result");
        
            List<Map<String, Object>> registros =
                    (List<Map<String, Object>>) result.get("records");

            for (Map<String, Object> row : registros) {
            
                Long ideConjUndConsumidoras = Long.valueOf(row.get("IdeConjUndConsumidoras").toString());
                String dscConjUndConsumidoras = row.get("DscConjUndConsumidoras").toString();
                String numCnpj = row.get("NumCNPJ").toString().replaceAll("[^0-9]", "");
                
                String sigIndicador = row.get("SigIndicador").toString();
                Long numPeriodoIndice = Long.valueOf(row.get("NumPeriodoIndice").toString());
                Long anoIndice = Long.valueOf(row.get("AnoIndice").toString());
                Double vlrIndiceEnviado = Double.valueOf(
                    row.get("VlrIndiceEnviado")
                       .toString()
                       .replace(",", ".")
                );
                LocalDate dataGeracaoConjDados = LocalDate.parse(
                    row.get("DatGeracaoConjuntoDados").toString()
                );

                LocalDate dataColeta = LocalDate.now();
            
                Conjunto conjunto = conjuntoRepository
                .findByIdeConjUndConsumidoras(ideConjUndConsumidoras)
                .orElse(null);
            
                boolean created = false;
            
                if (conjunto == null) {

            
                    Distribuidora dist = distribuidoraRepository
                        .findByNumCnpj(numCnpj)
                        .orElseThrow();
                    
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
                
                IndicadorType tipo = IndicadorType.valueOf(
                    sigIndicador.trim().toUpperCase()
                );

                SigIndicador indicador = sigIndicadorRepository
                .findByIndicadorType(tipo)
                .orElseThrow();
                
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
                
                } else {
                
                    throw new RuntimeException(
                        String.format(
                            "Métrica já existe para Conjunto %d, Indicador %s, Período %d, Ano %d",
                            conjunto.getId(),
                            indicador.getIndicadorType(),
                            numPeriodoIndice,
                            anoIndice
                        )
                    );
                }
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
