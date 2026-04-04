package com.energia.backend.etl.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConjuntoMetricasImport {
    
    public String importar (List<String> cnpjs){
        String baseUrl = "https://dadosabertos.aneel.gov.br/api/3/action/datastore_search_sql";

        String cnpjList = cnpjs.stream()
                .map(cnpj -> "'" + cnpj.replaceAll("[^0-9]", "") + "'")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    
        String sql = String.format(
            "SELECT * " +
            "FROM \"4493985c-baea-429c-9df5-3030422c71d7\" " +
            "WHERE (\"SigIndicador\" = 'FEC' OR \"SigIndicador\" = 'DEC') " +
            "AND \"NumCNPJ\" IN (%s)",
            cnpjList
        );
    
        String url = baseUrl + "?sql=" + sql;
    
        System.out.println(url); 

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        System.out.printf("RESPONSE: ", response);
        return response;
    }
    
}
