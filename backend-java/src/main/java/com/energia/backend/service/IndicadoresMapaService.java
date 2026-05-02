package com.energia.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.energia.backend.dto.MapaCalorConjuntoResponse;
import com.energia.backend.dto.MapaCalorIndicadorResponse;
import com.energia.backend.dto.MapaCalorResponse;
import com.energia.backend.repository.aneel.ConjuntoRepository;
import com.energia.backend.repository.aneel.projection.MapaCalorConjuntoProjection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class IndicadoresMapaService {

    private static final String SUBESTACAO_NAO_INFORMADA = "Nao informado";
    private static final String PERIODO_NAO_INFORMADO = "Sem referencia";

    private final ConjuntoRepository conjuntoRepository;
    private final ObjectMapper objectMapper;

    public IndicadoresMapaService(ConjuntoRepository conjuntoRepository, ObjectMapper objectMapper) {
        this.conjuntoRepository = conjuntoRepository;
        this.objectMapper = objectMapper;
    }

    public MapaCalorResponse obterMapaCalor(Long ano) {
        List<MapaCalorConjuntoProjection> dados = conjuntoRepository.buscarDadosMapaCalor(ano);
        List<MapaCalorConjuntoResponse> conjuntos = new ArrayList<>();

        for (MapaCalorConjuntoProjection item : dados) {
            JsonNode geometry = parseGeometry(item.getGeometryGeojson());
            if (geometry == null) {
                continue;
            }

            MapaCalorIndicadorResponse indicadorDec = indicador(
                    "dec",
                    "DEC",
                    item.getDecValor(),
                    item.getDecLim());
            MapaCalorIndicadorResponse indicadorFec = indicador(
                    "fec",
                    "FEC",
                    item.getFecValor(),
                    item.getFecLim());

            MapaCalorIndicadorResponse indicadorPrincipal = indicadorMedia(indicadorDec, indicadorFec);
            String criticidade = classificarCriticidade(indicadorPrincipal);

            List<MapaCalorIndicadorResponse> complementares = List.of(
                    indicador(
                            "perdas-nao-tecnicas",
                            "Perdas nao tecnicas",
                            item.getPerdasNaoTec(),
                            0d),
                    indicador(
                            "custo-perdas-nao-tecnicas",
                            "Custo perdas nao tecnicas",
                            item.getCustoPerdasNaoTec(),
                            0d));

            conjuntos.add(new MapaCalorConjuntoResponse(
                    String.valueOf(item.getIdeConjUndConsumidoras()),
                    coalesce(item.getDscConjUndConsumidoras(), "Conjunto sem nome"),
                    coalesce(item.getRazaoSocial(), "Distribuidora nao informada"),
                    coalesce(normalizeUf(item.getUf()), "N/A"),
                    SUBESTACAO_NAO_INFORMADA,
                    criticidade,
                    indicadorPrincipal,
                    List.of(indicadorDec, indicadorFec),
                    complementares,
                    formatarPeriodo(item.getPeriodoReferencia(), item.getAnoReferencia()),
                    geometry));
        }

        return new MapaCalorResponse(conjuntoRepository.listarAnosDisponiveisMapaCalor(), conjuntos);
    }

    private JsonNode parseGeometry(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(geoJson);
        } catch (Exception ignored) {
            return null;
        }
    }

    private MapaCalorIndicadorResponse indicador(String id, String label, Double valor, Double limite) {
        return new MapaCalorIndicadorResponse(id, label, safeNumber(valor), safeNumber(limite));
    }

    private MapaCalorIndicadorResponse indicadorMedia(
            MapaCalorIndicadorResponse dec,
            MapaCalorIndicadorResponse fec
    ) {
        return new MapaCalorIndicadorResponse(
                "media-dec-fec",
                "Media DEC/FEC",
                media(dec.getValor(), fec.getValor()),
                media(dec.getLimite(), fec.getLimite()));
    }

    private String classificarCriticidade(MapaCalorIndicadorResponse indicador) {
        double ratio = calcularRazao(indicador.getValor(), indicador.getLimite());

        if (ratio < 0) {
            return "ausente";
        }

        if (ratio >= 1d) {
            return "alto";
        }

        if (ratio >= 0.5d) {
            return "moderado";
        }

        return "baixo";
    }

    private double calcularRazao(double valor, double limite) {
        if (limite <= 0d) {
            return -1d;
        }

        return valor / limite;
    }

    private double safeNumber(Double value) {
        return value == null ? 0d : value;
    }

    private double media(double primeiro, double segundo) {
        return (primeiro + segundo) / 2d;
    }

    private String formatarPeriodo(Long periodo, Long ano) {
        if (ano == null) {
            return PERIODO_NAO_INFORMADO;
        }

        if (periodo == null) {
            return String.valueOf(ano);
        }

        return String.format(Locale.ROOT, "%02d/%d", periodo, ano);
    }

    private String coalesce(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }

    private String normalizeUf(String uf) {
        if (uf == null || uf.isBlank()) {
            return null;
        }

        return uf.trim().toUpperCase(Locale.ROOT);
    }
}
