package com.energia.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.CriticidadeMapaResponse;
import com.energia.backend.repository.aneel.MetricasRepository;
import com.energia.backend.repository.aneel.projection.CriticidadeMapaProjection;

@Service
public class IndicadorMapaService {

    private final MetricasRepository metricasRepository;

    public IndicadorMapaService(MetricasRepository metricasRepository) {
        this.metricasRepository = metricasRepository;
    }

    @Transactional(readOnly = true)
    public List<CriticidadeMapaResponse> buscarCriticidade(Long mes, Long ano, String nomeConjunto) {
        validarParametros(mes, ano);

        String filtroNomeConjunto = normalizarNomeConjunto(nomeConjunto);

        return metricasRepository.buscarCriticidadeParaMapa(mes, ano, filtroNomeConjunto)
                .stream()
                .filter(item -> item.getIndiceCriticidadePercentual() != null)
                .map(this::toResponse)
                .toList();
    }

    private CriticidadeMapaResponse toResponse(CriticidadeMapaProjection item) {
        double indice = arredondarDuasCasas(item.getIndiceCriticidadePercentual());
        return new CriticidadeMapaResponse(
                item.getNomeConjunto(),
                indice,
                calcularFaixa(indice)
        );
    }

    private void validarParametros(Long mes, Long ano) {
        if (mes == null || mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Parametro 'mes' deve estar entre 1 e 12.");
        }

        if (ano == null || ano < 1900) {
            throw new IllegalArgumentException("Parametro 'ano' deve ser maior ou igual a 1900.");
        }
    }

    private String normalizarNomeConjunto(String nomeConjunto) {
        if (nomeConjunto == null) {
            return null;
        }

        String nomeNormalizado = nomeConjunto.trim();
        return nomeNormalizado.isEmpty() ? null : nomeNormalizado;
    }

    private String calcularFaixa(double indiceCriticidadePercentual) {
        if (indiceCriticidadePercentual < 50.0) {
            return "VERDE";
        }

        if (indiceCriticidadePercentual <= 100.0) {
            return "AMARELO";
        }

        return "VERMELHO";
    }

    private double arredondarDuasCasas(Double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
