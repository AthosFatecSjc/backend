package com.energia.backend.dto;

import java.util.List;

public class MapaCalorResponse {
    private List<Long> anosDisponiveis;
    private List<Long> mesesDisponiveis;
    private List<MapaCalorConjuntoResponse> conjuntos;

    public MapaCalorResponse() {
    }

    public MapaCalorResponse(List<Long> anosDisponiveis, List<MapaCalorConjuntoResponse> conjuntos) {
        this(anosDisponiveis, List.of(), conjuntos);
    }

    public MapaCalorResponse(
            List<Long> anosDisponiveis,
            List<Long> mesesDisponiveis,
            List<MapaCalorConjuntoResponse> conjuntos
    ) {
        this.anosDisponiveis = anosDisponiveis;
        this.mesesDisponiveis = mesesDisponiveis;
        this.conjuntos = conjuntos;
    }

    public List<Long> getAnosDisponiveis() {
        return anosDisponiveis;
    }

    public void setAnosDisponiveis(List<Long> anosDisponiveis) {
        this.anosDisponiveis = anosDisponiveis;
    }

    public List<Long> getMesesDisponiveis() {
        return mesesDisponiveis;
    }

    public void setMesesDisponiveis(List<Long> mesesDisponiveis) {
        this.mesesDisponiveis = mesesDisponiveis;
    }

    public List<MapaCalorConjuntoResponse> getConjuntos() {
        return conjuntos;
    }

    public void setConjuntos(List<MapaCalorConjuntoResponse> conjuntos) {
        this.conjuntos = conjuntos;
    }
}
