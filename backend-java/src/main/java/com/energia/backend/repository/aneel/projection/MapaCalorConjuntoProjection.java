package com.energia.backend.repository.aneel.projection;

public interface MapaCalorConjuntoProjection {
    Long getConjuntoId();

    Long getIdeConjUndConsumidoras();

    String getDscConjUndConsumidoras();

    String getRazaoSocial();

    String getUf();

    Long getAnoReferencia();

    Long getPeriodoReferencia();

    Double getDecValor();

    Double getFecValor();

    Double getDecLim();

    Double getFecLim();

    Double getPerdasNaoTec();

    Double getCustoPerdasNaoTec();

    String getGeometryGeojson();
}