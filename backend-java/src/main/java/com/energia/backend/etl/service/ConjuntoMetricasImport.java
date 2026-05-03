package com.energia.backend.etl.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConjuntoMetricasImport {
    
    public String importar (String cnpj){
        String baseUrl = "https://dadosabertos.aneel.gov.br/api/3/action/datastore_search_sql";

    
        String sql = String.format(
            "SELECT * " +
            "FROM \"4493985c-baea-429c-9df5-3030422c71d7\" " +
            "WHERE (\"SigIndicador\" = 'FEC' OR \"SigIndicador\" = 'DEC') " +
            "AND \"NumCNPJ\" = '%s'",
            cnpj
        );
    
        String url = baseUrl + "?sql=" + sql;
    

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        return response;
    }
    
}
