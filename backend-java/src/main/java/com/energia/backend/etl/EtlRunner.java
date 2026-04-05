package com.energia.backend.etl;

import java.util.Arrays;
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
        ConjMetricTransformLoad conjTransformService){
        return args -> {
            if (Arrays.stream(args).noneMatch(arg -> arg.equalsIgnoreCase("etl"))) {
                System.out.println("ARGS: " + Arrays.toString(args));
                System.out.println("ENTROU NA FUNÇÃO DO ETL");
                return;
            }
            System.out.println("ENTROU NO ETL");

            try{
                String caminho = System.getenv("ETL_FILE_DIST_PATH"); 
                if (caminho == null || caminho.isBlank()) {
                    throw new RuntimeException("ETL_FILE_DIST_PATH não está definida");
                }
                distService.importar(caminho);
                List<String> cnpjs = List.of("97578090000134"); 
                String json = conjService.importar(cnpjs);
                conjTransformService.processarJson(json);

                System.out.println("ETL REALIZADO");
            }catch (Exception e) {
                System.err.println("ERRO DURANTE ETL");
                e.printStackTrace();
                System.exit(1); 
            };
            System.exit(0);
        };
       }
}