package com.energia.backend.etl;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.energia.backend.etl.service.PerdasExcelImport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class PerdasEtlRunner {

    private final ApplicationArguments args;

    @Bean
    CommandLineRunner runPerdas(PerdasExcelImport perdasImport) {
        return cliArgs -> {
            boolean isEtlPerdas = args.getNonOptionArgs().stream()
                    .anyMatch(arg -> arg.equalsIgnoreCase("etl-perdas"));

            if (!isEtlPerdas) {
                return;
            }

            log.info("Iniciando ETL de perdas...");

            try {
                perdasImport.importar();
                log.info("ETL de perdas concluído com sucesso.");
            } catch (Exception e) {
                log.error("ETL de perdas falhou: {}", e.getMessage(), e);
                System.exit(1);
            }

            System.exit(0);
        };
    }
}
