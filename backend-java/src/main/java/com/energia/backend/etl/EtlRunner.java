package com.energia.backend.etl;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.energia.backend.etl.service.DistribuidoraExcelImport;
import com.energia.backend.etl.service.LimitesImport;
import com.energia.backend.etl.service.LimitesTransformLoad;
import com.energia.backend.etl.service.ConjuntoMetricasImport;
import com.energia.backend.etl.LimitesCsvParser.LimiteFiltrado;
import com.energia.backend.etl.service.ConjMetricTransformLoad;
import com.energia.backend.etl.service.AneelExtractionLoggingService;
import com.energia.backend.etl.exception.DuplicatesDetectedException;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class EtlRunner {

    private static final String DEFAULT_CNPJ = "97578090000134";

    @Bean
    CommandLineRunner run(DistribuidoraExcelImport distService,
        LimitesCsvParser limitesCsvParser,
        ConjuntoMetricasImport conjService,
        LimitesImport limService,
        LimitesTransformLoad limitesTransformLoad,
        ConjMetricTransformLoad conjTransformService,
        AneelExtractionLoggingService loggingService){
        return args -> {
            if (Arrays.stream(args).noneMatch(arg -> arg.equalsIgnoreCase("etl"))) {
                System.out.println("ARGS: " + Arrays.toString(args));
                System.out.println("ENTROU NA FUNÇÃO DO ETL");
                return;
            }
            System.out.println("ENTROU NO ETL");

            loggingService.logExtractionStart();

            List<String> errors = new ArrayList<>();

            try {
                String caminho = System.getenv("ETL_FILE_DIST_PATH");
                if (caminho == null || caminho.isBlank()) {
                    throw new RuntimeException("ETL_FILE_DIST_PATH não está definida");
                }

                try {
                    distService.importar(caminho);
                } catch (Exception e) {
                    errors.add("Erro ao importar distribuidoras: " + e.getMessage());
                }
                List<Long> conjuntos = new ArrayList<>();

                try {
                    List<String> cnpjs = getTargetCnpjs();
                    String json = conjService.importar(cnpjs);
                    conjuntos = conjTransformService.processarJson(json);
                } catch (DuplicatesDetectedException e) {
                    loggingService.logExtractionFailDuplicata(e.getCount());
                    errors.add("Duplicatas detectadas em métricas: " + e.getCount());
                } catch (Exception e) {
                    errors.add("Erro ao processar métricas de conjunto: " + e.getMessage());
                }

                try {
                    String response = limService.importar();
                    List<LimiteFiltrado> listaLim = limitesCsvParser.parsearFiltrando(response, conjuntos);
                    limitesTransformLoad.processarLista(listaLim);
                } catch (Exception e) {
                    errors.add("Erro ao processar limites: " + e.getMessage());
                }

                System.out.println("ETL REALIZADO");

                if (!errors.isEmpty()) {
                    loggingService.logExtractionFail(
                        "ETL finalizado com " + errors.size() + " erro(s): " +
                        String.join("; ", errors)
                    );
                    System.exit(1);
                } else {
                    loggingService.logExtractionSuccess("Todos os dados ANEEL foram extraídos e carregados com sucesso");
                }

            } catch (Exception e) {
                loggingService.logExtractionFail("ERRO DURANTE ETL ANEEL (possível extração vazia/ inválida) " + e.getMessage());
                System.exit(1);
            }
            System.exit(0);
        };
       }

    private List<String> getTargetCnpjs() {
        String raw = System.getenv("ETL_CNPJS");
        if (raw == null || raw.isBlank()) {
            return List.of(DEFAULT_CNPJ);
        }

        List<String> cnpjs = Arrays.stream(raw.split(","))
            .map(Utils::formatCnpj)
            .filter(cnpj -> cnpj != null && !cnpj.isBlank())
            .distinct()
            .toList();

        if (cnpjs.isEmpty()) {
            throw new IllegalArgumentException("ETL_CNPJS não contém CNPJs válidos");
        }
        return cnpjs;
    }

    // private List<Long> getTargetConjuntos() {
    //     String raw = System.getenv("ETL_CONJUNTOS_IDS");
    //     if (raw == null || raw.isBlank()) {
    //         return List.of(DEFAULT_CONJUNTO_ID);
    //     }

    //     List<Long> conjuntos = Arrays.stream(raw.split(","))
    //         .map(String::trim)
    //         .map(Utils::toLong)
    //         .filter(id -> id != null)
    //         .distinct()
    //         .toList();

    //     if (conjuntos.isEmpty()) {
    //         throw new IllegalArgumentException("ETL_CONJUNTOS_IDS não contém IDs válidos");
    //     }
    //     return conjuntos;
    // }
}