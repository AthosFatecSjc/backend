package com.energia.backend.repository.aneel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.IndicadorType;
import com.energia.backend.model.aneel.SigIndicador;

public interface SigIndicadorRepository extends JpaRepository<SigIndicador, Long> {
    Optional<SigIndicador> findByIndicadorType(IndicadorType tipo);
}