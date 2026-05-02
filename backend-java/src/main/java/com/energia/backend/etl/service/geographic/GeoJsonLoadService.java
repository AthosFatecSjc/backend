package com.energia.backend.etl.service.geographic;

import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.wololo.jts2geojson.GeoJSONReader;

import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.Distribuidora;
import com.energia.backend.repository.aneel.ConjuntoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoJsonLoadService {

    private final ConjuntoRepository conjuntoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void importar(String geoJsonContent, Distribuidora dist)
            throws IOException, JsonMappingException, JsonProcessingException {

        JsonNode root = objectMapper.readTree(geoJsonContent);
        JsonNode features = root.path("features");

        for (JsonNode feature : features) {

            JsonNode props = feature.path("properties");

            Long codId = props.path("COD_ID").asLong();
            System.out.println("O CODID DESSE CONJUNTO É: " + codId);

            Optional<Conjunto> optConjunto = conjuntoRepository.findByIdeConjUndConsumidoras(codId);

            if (optConjunto.isEmpty()) {
                System.out.println("NÃO FOI ACHADO CONJUNTO COM CODID= " + codId);
                continue;
            }

            JsonNode geometryNode = feature.path("geometry");

            Geometry geometry = converterGeometry(geometryNode);

            conjuntoRepository.atualizarGeometria(codId, geometry);
        }
    }

    public Geometry converterGeometry(JsonNode geometryNode) {
        try {
            GeoJSONReader reader = new GeoJSONReader();
            Geometry geometry = reader.read(geometryNode.toString());

            geometry.setSRID(4674);

            return geometry;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter geometria", e);
        }
    }
}