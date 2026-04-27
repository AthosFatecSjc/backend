package com.energia.backend.dto;

public class CriticidadeMapaResponse {

    private String nomeConjunto;
    private Double indiceCriticidadePercentual;
    private String faixa;

    public CriticidadeMapaResponse() {
    }

    public CriticidadeMapaResponse(String nomeConjunto, Double indiceCriticidadePercentual, String faixa) {
        this.nomeConjunto = nomeConjunto;
        this.indiceCriticidadePercentual = indiceCriticidadePercentual;
        this.faixa = faixa;
    }

    public String getNomeConjunto() {
        return nomeConjunto;
    }

    public void setNomeConjunto(String nomeConjunto) {
        this.nomeConjunto = nomeConjunto;
    }

    public Double getIndiceCriticidadePercentual() {
        return indiceCriticidadePercentual;
    }

    public void setIndiceCriticidadePercentual(Double indiceCriticidadePercentual) {
        this.indiceCriticidadePercentual = indiceCriticidadePercentual;
    }

    public String getFaixa() {
        return faixa;
    }

    public void setFaixa(String faixa) {
        this.faixa = faixa;
    }
}
