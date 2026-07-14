package com.energia.backend.etl.service;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ConjuntoMetricasImport {

    public String importar(String cnpj) {
        String baseUrl = "https://dadosabertos.aneel.gov.br/api/action/datastore_search";
        String resourceId = "4493985c-baea-429c-9df5-3030422c71d7";

        String filters = String.format(
            "{\"NumCNPJ\": \"%s\", \"SigIndicador\": [\"DEC\", \"FEC\"]}",
            cnpj
        );

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .queryParam("resource_id", resourceId)
            .queryParam("filters", filters)
            .queryParam("limit", 32000)
            .build()
            .encode()
            .toUri();

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(uri, String.class);
        return response;
    }

}
