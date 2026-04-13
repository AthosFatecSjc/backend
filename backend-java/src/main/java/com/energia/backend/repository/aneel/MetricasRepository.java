package com.energia.backend.repository.aneel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.Metricas;
import com.energia.backend.model.aneel.SigIndicador;

public interface MetricasRepository extends JpaRepository<Metricas, Long> {
    Optional<Metricas> findByConjuntoAndSigIndicadorAndNumPeriodoIndiceAndAnoIndice(
        Conjunto conjunto,
        SigIndicador sigIndicador,
        Long numPeriodoIndice,
        Long anoIndice
    );
}