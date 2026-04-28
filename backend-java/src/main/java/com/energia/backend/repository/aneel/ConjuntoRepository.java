package com.energia.backend.repository.aneel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.energia.backend.model.aneel.Conjunto;

public interface ConjuntoRepository extends JpaRepository<Conjunto, Long> {
    Optional<Conjunto> findByIdeConjUndConsumidoras(Long ide);

    @Query(value = """
                UPDATE aneel.conjunto
                SET geometry = ST_SetSRID(ST_GeomFromGeoJSON(:geojson), 4674)
                WHERE ide_conj_und_consumidoras = :codId
            """, nativeQuery = true)
    void atualizarGeometria(Long codId, String geojson);
}