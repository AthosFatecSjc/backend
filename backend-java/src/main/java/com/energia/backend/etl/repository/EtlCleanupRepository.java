package com.energia.backend.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.energia.backend.model.aneel.Conjunto;

import jakarta.transaction.Transactional;

@Repository
public interface EtlCleanupRepository extends JpaRepository<Conjunto, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        TRUNCATE TABLE aneel.metricas, aneel.limites, aneel.conjunto RESTART IDENTITY CASCADE
    """, nativeQuery = true)
    void limparTabelas();
}