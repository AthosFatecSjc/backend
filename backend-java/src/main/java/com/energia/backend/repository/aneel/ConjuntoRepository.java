package com.energia.backend.repository.aneel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.Conjunto;

public interface ConjuntoRepository extends JpaRepository<Conjunto, Long> {
    Optional<Conjunto> findByIdeConjUndConsumidoras(Long ide);
}