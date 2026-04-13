package com.energia.backend.repository.aneel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.Limites;

public interface LimitesRepository extends JpaRepository<Limites, Long> {

    Optional<Limites> findByConjuntoAndAno(Conjunto conjunto, Long anoLimiteQualidade);
}