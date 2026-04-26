package com.energia.backend.etl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;



@Service
@Slf4j
public class LimitesCsvParser {

    public record LimiteFiltrado(
            Long ideConjUndConsumidoras,
            String sigIndicador,
            Long anoLimiteQualidade,
            Double vlrLimite,
            LocalDate dataGeracao
    ) {}

    public List<LimiteFiltrado> parsearFiltrando(String csv, List<Long> ideConjPermitidos) throws IOException {
        Set<Long> permitidos = new HashSet<>(ideConjPermitidos);
        List<LimiteFiltrado> linhas = new ArrayList<>();
        int linhasComDado = 0;

        try (BufferedReader br = new BufferedReader(new StringReader(csv))) {
            String line;
            boolean primeiraLinha = true;

            while ((line = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false; 
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }
                linhasComDado++;

                String[] cols = line.split(";", -1);
                if (cols.length < 8) {
                    log.warn("Linha de limites ignorada por quantidade de colunas inválida: {}", line);
                    continue; 
                }

                Long ideConj = Utils.toLong(cols[3].trim());
                String sigIndicador = Utils.cleanNullable(cols[5].trim());
                Long ano = Utils.toLong(cols[6].trim());
                Double valor = Utils.toDoubleBrNullable(cols[7].trim());
                LocalDate dataGeracao = Utils.toDateNullable(cols[0].trim());

                if (sigIndicador == null || ano == null) {
                    log.warn("Linha de limites ignorada por campos obrigatórios inválidos: {}", line);
                    continue;
                }

                if (ideConj == null || !permitidos.contains(ideConj)) {
                    continue; 
                }

                linhas.add(new LimiteFiltrado(
                        ideConj,
                        sigIndicador,
                        ano,
                        valor,
                        dataGeracao
                ));
            }
        }

        if (linhasComDado == 0 || linhas.isEmpty()) {
            throw new IllegalStateException("Erro na extração ANEEL: limites vazio ou sem registros válidos");
        }

        log.info("Limites válidos processados: {}", linhas.size());
        return linhas;
    }
}