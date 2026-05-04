package com.energia.backend.etl.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.energia.backend.etl.LimitesCsvParser.LimiteFiltrado;
import com.energia.backend.model.aneel.ColetaDados;
import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.DataKey;
import com.energia.backend.model.aneel.Limites;
import com.energia.backend.repository.aneel.ColetaDadosRepository;
import com.energia.backend.repository.aneel.ConjuntoRepository;
import com.energia.backend.repository.aneel.LimitesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LimitesTransformLoad {

    private final ConjuntoRepository conjuntoRepository;
    private final ColetaDadosRepository coletaDadosRepository;
    private final LimitesRepository limitesRepository;

    @Transactional
    public void processarLista(List<LimiteFiltrado> listaLim) {
        int processados = 0;
        for (LimiteFiltrado lim : listaLim) {
            if (lim == null || lim.anoLimiteQualidade() == null || lim.sigIndicador() == null) {
                log.warn("Registro de limite inválido ignorado: {}", lim);
                continue;
            }

            Optional<Conjunto> conjuntoOpt = conjuntoRepository.findByIdeConjUndConsumidoras(lim.ideConjUndConsumidoras());
            if (conjuntoOpt.isEmpty()) {
                log.warn("Conjunto não encontrado para ideConjUndConsumidoras={}", lim.ideConjUndConsumidoras());
                continue; 
            }
            Conjunto conjunto = conjuntoOpt.get();
            Optional<Limites> existente = limitesRepository.findByConjuntoAndAno(conjunto, lim.anoLimiteQualidade());
            Long id;
            if (existente.isPresent()) {
                Limites limExistente = existente.get();
            
                if (lim.sigIndicador().equalsIgnoreCase("DEC") 
                    && limExistente.getDecLim() == null) {
                    limExistente.setDecLim(lim.vlrLimite());
                }
                if (lim.sigIndicador().equalsIgnoreCase("FEC") 
                    && limExistente.getFecLim() == null) {
                    limExistente.setFecLim(lim.vlrLimite());
                }
                limitesRepository.save(limExistente);
                processados++;
                
            }
            else {
                Limites novo = new Limites();
                novo.setConjunto(conjunto);
                novo.setAno(lim.anoLimiteQualidade());
            
                if (lim.sigIndicador().equalsIgnoreCase("DEC")) {
                    novo.setDecLim(lim.vlrLimite());
                } else if (lim.sigIndicador().equalsIgnoreCase("FEC")) {
                    novo.setFecLim(lim.vlrLimite());
                } else {
                    log.warn("SigIndicador de limite inválido: {}", lim.sigIndicador());
                    continue;
                }
            
                limitesRepository.save(novo);
                processados++;
                id = novo.getId();

                ColetaDados coleta = new ColetaDados();
                coleta.setDataColeta(LocalDate.now());
                coleta.setDataGeracao(lim.dataGeracao());
                coleta.setDataKey(DataKey.LIMITES);
                coleta.setIdData(id);
                coleta.setLink("https://dadosabertos.aneel.gov.br/dataset/indicadores-coletivos-de-continuidade-dec-e-fec/resource/fd69e1dd-fd66-4269-b60c-cc0b7eb221b4");
                coletaDadosRepository.save(coleta);
            }
        
                        
        }

        if (processados == 0) {
            throw new IllegalStateException("Erro na extração ANEEL: nenhum registro válido de limites foi processado");
        }
    }
    
}
