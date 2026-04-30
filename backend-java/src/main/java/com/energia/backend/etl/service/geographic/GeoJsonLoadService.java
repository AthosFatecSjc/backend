package com.energia.backend.etl.service.geographic;

import java.io.File;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.wololo.jts2geojson.GeoJSONReader;

import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.Distribuidora;
import com.energia.backend.repository.aneel.ConjuntoRepository;
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
    public void importar(File geoJsonFile, Distribuidora dist) throws IOException, java.io.IOException {

        JsonNode root = objectMapper.readTree(geoJsonFile);
        JsonNode features = root.path("features");

        for (JsonNode feature : features) {

            JsonNode props = feature.path("properties");

            Long codId = props.path("COD_ID").asLong();

            Optional<Conjunto> optConjunto = conjuntoRepository.findByIdeConjUndConsumidoras(codId);

            if (optConjunto.isEmpty()) {
                continue;
            }

            String geojson = feature.path("geometry").toString();

            conjuntoRepository.atualizarGeometria(codId, geojson);
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