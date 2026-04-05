package com.energia.backend.etl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;


@Service
public class LimitesCsvParser {

    public record LimiteFiltrado(
            Long ideConjUndConsumidoras,
            String sigIndicador,
            Long anoLimiteQualidade,
            Double vlrLimite
    ) {}

    public List<LimiteFiltrado> parsearFiltrando(String csv, List<Long> ideConjPermitidos) throws IOException {
        Set<Long> permitidos = new HashSet<>(ideConjPermitidos);
        List<LimiteFiltrado> linhas = new ArrayList<>();

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

                String[] cols = line.split(";", -1);
                if (cols.length < 8) {
                    continue; 
                }

                Long ideConj = Utils.toLong(cols[3].trim());
                String sigIndicador = cols[5].trim();
                Long ano = Utils.toLong(cols[6].trim());
                Double valor = parseDoubleBr(cols[7].trim());

                if (ideConj == null || !permitidos.contains(ideConj)) {
                    continue; 
                }

                linhas.add(new LimiteFiltrado(
                        ideConj,
                        sigIndicador,
                        ano,
                        valor
                ));
            }
        }
        System.out.println("Linhas: " + linhas);
        return linhas;
    }

    private Double parseDoubleBr(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Double.parseDouble(raw.replace(",", "."));
    }
}