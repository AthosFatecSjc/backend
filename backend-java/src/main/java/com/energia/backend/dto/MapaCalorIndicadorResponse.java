package com.energia.backend.dto;

public class MapaCalorIndicadorResponse {
    private String id;
    private String label;
    private double valor;
    private double limite;

    public MapaCalorIndicadorResponse() {
    }

    public MapaCalorIndicadorResponse(String id, String label, double valor, double limite) {
        this.id = id;
        this.label = label;
        this.valor = valor;
        this.limite = limite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public double getLimite() {
        return limite;
    }

    public void setLimite(double limite) {
        this.limite = limite;
    }
}