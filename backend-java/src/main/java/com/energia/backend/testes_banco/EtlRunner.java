package com.energia.backend.testes_banco;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.energia.backend.etl.service.DistribuidoraExcelImport;
import com.energia.backend.etl.service.ConjuntoMetricasImport;
import com.energia.backend.etl.service.ConjMetricTransformLoad;

@Configuration
public class EtlRunner {

    @Bean
    CommandLineRunner run(DistribuidoraExcelImport distService, 
        ConjuntoMetricasImport conjService, 
        ConjMetricTransformLoad conjTransformService) {
        return args -> {
            System.out.println("🔥 ENTROU NO ETL");
            String caminho = "/app/DistBase.xlsx"; 
            distService.importar(caminho);
            List<String> cnpjs = List.of("97578090000134"); 
            String json = conjService.importar(cnpjs);
            conjTransformService.processarJson(json);

            System.out.println("FIM DO ETL");
        };
    }
}