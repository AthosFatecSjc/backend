package com.energia.backend.repository.aneel;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.Perdas;

public interface PerdasRepository extends JpaRepository<Perdas, Long> {
}