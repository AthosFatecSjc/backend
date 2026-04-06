package com.energia.backend.repository.aneel;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.Limites;

public interface LimitesRepository extends JpaRepository<Limites, Long> {
}