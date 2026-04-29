package com.energia.backend.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class MapaCalorConjuntoResponse {
    private String id;
    private String nome;
    private String distribuidora;
    private String estado;
    private String subestacao;
    private String criticidade;
    private MapaCalorIndicadorResponse indicadorPrincipal;
    private List<MapaCalorIndicadorResponse> indicadoresPrincipais;
    private List<MapaCalorIndicadorResponse> complementares;
    private String periodoReferencia;
    private JsonNode geometry;

    public MapaCalorConjuntoResponse() {
    }

    public MapaCalorConjuntoResponse(
            String id,
            String nome,
            String distribuidora,
            String estado,
            String subestacao,
            String criticidade,
            MapaCalorIndicadorResponse indicadorPrincipal,
            List<MapaCalorIndicadorResponse> indicadoresPrincipais,
            List<MapaCalorIndicadorResponse> complementares,
            String periodoReferencia,
            JsonNode geometry
    ) {
        this.id = id;
        this.nome = nome;
        this.distribuidora = distribuidora;
        this.estado = estado;
        this.subestacao = subestacao;
        this.criticidade = criticidade;
        this.indicadorPrincipal = indicadorPrincipal;
        this.indicadoresPrincipais = indicadoresPrincipais;
        this.complementares = complementares;
        this.periodoReferencia = periodoReferencia;
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDistribuidora() {
        return distribuidora;
    }

    public void setDistribuidora(String distribuidora) {
        this.distribuidora = distribuidora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSubestacao() {
        return subestacao;
    }

    public void setSubestacao(String subestacao) {
        this.subestacao = subestacao;
    }

    public String getCriticidade() {
        return criticidade;
    }

    public void setCriticidade(String criticidade) {
        this.criticidade = criticidade;
    }

    public MapaCalorIndicadorResponse getIndicadorPrincipal() {
        return indicadorPrincipal;
    }

    public void setIndicadorPrincipal(MapaCalorIndicadorResponse indicadorPrincipal) {
        this.indicadorPrincipal = indicadorPrincipal;
    }

    public List<MapaCalorIndicadorResponse> getIndicadoresPrincipais() {
        return indicadoresPrincipais;
    }

    public void setIndicadoresPrincipais(List<MapaCalorIndicadorResponse> indicadoresPrincipais) {
        this.indicadoresPrincipais = indicadoresPrincipais;
    }

    public List<MapaCalorIndicadorResponse> getComplementares() {
        return complementares;
    }

    public void setComplementares(List<MapaCalorIndicadorResponse> complementares) {
        this.complementares = complementares;
    }

    public String getPeriodoReferencia() {
        return periodoReferencia;
    }

    public void setPeriodoReferencia(String periodoReferencia) {
        this.periodoReferencia = periodoReferencia;
    }

    public JsonNode getGeometry() {
        return geometry;
    }

    public void setGeometry(JsonNode geometry) {
        this.geometry = geometry;
    }
}