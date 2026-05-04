package com.energia.backend.repository.aneel;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.ColetaDados;

public interface ColetaDadosRepository extends JpaRepository<ColetaDados, Long> {
    
}
