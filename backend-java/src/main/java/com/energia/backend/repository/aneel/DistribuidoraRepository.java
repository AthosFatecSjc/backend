package com.energia.backend.repository.aneel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.aneel.Distribuidora;

public interface DistribuidoraRepository extends JpaRepository<Distribuidora, Long> {
   
    boolean existsByCodigoIdDist(Long codigoIdDist);
    Optional<Distribuidora> findByNumCnpj(String numCnpj);
}