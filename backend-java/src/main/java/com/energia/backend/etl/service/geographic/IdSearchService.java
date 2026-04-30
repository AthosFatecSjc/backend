package com.energia.backend.etl.service.geographic;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdSearchService {
    private static final String HUB_SEARCH_URL = "https://hub.arcgis.com/api/search/v1/collections/all/items";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String localizarItemId(String sigAgente) {
        List<String> tentativas = gerarTentativasBusca(sigAgente);

        for (String tentativa : tentativas) {
            String url = montarUrlBusca(tentativa);
            try {
                String response = restTemplate.getForObject(url, String.class);
                if (response == null || response.isBlank()) {
                    continue;
                }
                String itemId = extrairItemMaisRecente(response);
                if (itemId != null) {
                    log.info("Item ArcGIS encontrado para {} com busca '{}': {}", sigAgente, tentativa, itemId);
                    return itemId;
                }
            } catch (Exception e) {
                log.warn("Falha ao buscar item ArcGIS com tentativa '{}': {}", tentativa, e.getMessage());
            }

        }

        return null;

    }

    private String montarUrlBusca(String sigAgente) {
        return HUB_SEARCH_URL
                + "?q=" + urlEncode(sigAgente)
                + "&limit=12";
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao codificar URL", e);
        }
    }

    private List<String> gerarTentativasBusca(String sigAgente) {
        String base = normalizarSigAgente(sigAgente);

        Set<String> tentativas = new HashSet<>();

        tentativas.add(base);
        tentativas.add(base.replace(" ", "_"));
        tentativas.add(base.replace(" ", ""));
        tentativas.add(base.replace(" ", "-"));

        return tentativas.stream()
                .filter(s -> s != null && !s.isBlank())
                .toList();
    }

    private String normalizarSigAgente(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();
    }

    private String extrairItemMaisRecente(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode features = root.path("features");

        if (!features.isArray() || features.isEmpty()) {
            return null;
        }

        String bestId = null;
        LocalDate bestDate = null;

        for (JsonNode feature : features) {

            JsonNode props = feature.path("properties");
            String type = props.path("type").asText();

            if (!"File Geodatabase".equalsIgnoreCase(type)) {
                continue;
            }

            String id = feature.path("id").asText(null);
            JsonNode tagsNode = props.path("tags");

            if (id == null || !tagsNode.isArray()) {
                continue;
            }

            LocalDate data = extrairDataDasTags(tagsNode);

            if (data == null) {
                continue;
            }

            if (bestDate == null || data.isAfter(bestDate)) {
                bestDate = data;
                bestId = id;
            }
        }

        log.info("Item mais recente -> id: {}, data: {}", bestId, bestDate);

        return bestId;
    }

    private LocalDate extrairDataDasTags(JsonNode tagsNode) {
        for (JsonNode tag : tagsNode) {
            String value = tag.asText();

            try {
                return LocalDate.parse(value);
            } catch (Exception e) {
            }
        }
        return null;
    }

}
