package com.energia.backend.etl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;

import com.energia.backend.etl.LimitesCsvParser.LimiteFiltrado;
import com.energia.backend.etl.exception.DuplicatesDetectedException;
import com.energia.backend.etl.repository.EtlCleanupRepository;
import com.energia.backend.etl.service.AneelExtractionLoggingService;
import com.energia.backend.etl.service.ConjMetricTransformLoad;
import com.energia.backend.etl.service.ConjuntoMetricasImport;
import com.energia.backend.etl.service.DistribuidoraExcelImport;
import com.energia.backend.etl.service.LimitesImport;
import com.energia.backend.etl.service.LimitesTransformLoad;
import com.energia.backend.etl.service.geographic.EtlService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class EtlRunner {

    private final ApplicationArguments args;
    private final EtlCleanupRepository cleanupRepository;
    private final EtlService importService;

    @Value("${etl_cnpj}")
    private String cnpj;

    @Bean
    CommandLineRunner run(DistribuidoraExcelImport distService,
            LimitesCsvParser limitesCsvParser,
            ConjuntoMetricasImport conjService,
            LimitesImport limService,
            LimitesTransformLoad limitesTransformLoad,
            ConjMetricTransformLoad conjTransformService,
            AneelExtractionLoggingService loggingService) {
        return cliArgs -> {
            boolean isEtl = args.getNonOptionArgs().stream()
                    .anyMatch(arg -> arg.equalsIgnoreCase("etl"));

            if (!isEtl) {
                System.out.println("ARGS: " + Arrays.toString(args.getSourceArgs()));
                System.out.println("ENTROU NA FUNÇÃO DO ETL");

                return;
            }
            System.out.println("ENTROU NO ETL");

            cleanupRepository.limparTabelas();

            System.out.println("O CNPJ É " + cnpj);

            if (cnpj == null || cnpj.isBlank()) {
                throw new IllegalArgumentException("Missing required argument: --cnpj=...");
            }

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
                    String cnpj_formatted = Utils.formatCnpj(cnpj);
                    String json = conjService.importar(cnpj_formatted);
                    conjuntos = conjTransformService.processarJson(json);
                } catch (Exception e) {
                    System.out.print("erro ao processar conjuntos e metricas");
                    errors.add("Erro ao processar conjunto e métricas: " + e.getMessage());
                }

                try {
                    String response = limService.importar();
                    List<LimiteFiltrado> listaLim = limitesCsvParser.parsearFiltrando(response, conjuntos);
                    limitesTransformLoad.processarLista(listaLim);
                } catch (Exception e) {
                    errors.add("Erro ao processar limites: " + e.getMessage());
                }

                try {
                    importService.executarPipeline();

                    
                } catch (Exception e) {
                    errors.add("Erro com etl de dados geográficos: " + e.getMessage());
                }

                System.out.println("ETL REALIZADO");

                if (!errors.isEmpty()) {
                    loggingService.logExtractionFail(
                            "ETL finalizado com " + errors.size() + " erro(s): " +
                                    String.join("; ", errors));
                    System.exit(1);
                } else {
                    loggingService
                            .logExtractionSuccess("Todos os dados ANEEL foram extraídos e carregados com sucesso");
                }

            } catch (Exception e) {
                loggingService.logExtractionFail(
                        "ERRO DURANTE ETL ANEEL (possível extração vazia/ inválida) " + e.getMessage());
                System.exit(1);
            }
            System.exit(0);
        };
    }

}