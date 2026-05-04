package com.energia.backend.repository.aneel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.model.aneel.Metricas;
import com.energia.backend.model.aneel.SigIndicador;
import com.energia.backend.repository.aneel.projection.CriticidadeMapaProjection;

public interface MetricasRepository extends JpaRepository<Metricas, Long> {
    Optional<Metricas> findByConjuntoAndSigIndicadorAndNumPeriodoIndiceAndAnoIndice(
        Conjunto conjunto,
        SigIndicador sigIndicador,
        Long numPeriodoIndice,
        Long anoIndice
    );

    @Query(value = """
        SELECT
            c.dsc_conj_und_consumidoras AS \"nomeConjunto\",
            (
                (
                    AVG(CASE WHEN si.indicador_type = 'DEC' THEN m.vlr_indice_enviado END) +
                    AVG(CASE WHEN si.indicador_type = 'FEC' THEN m.vlr_indice_enviado END)
                ) / 2.0
            ) / NULLIF(((l.dec_lim + l.fec_lim) / 2.0), 0) * 100.0 AS \"indiceCriticidadePercentual\"
        FROM aneel.metricas m
        JOIN aneel.conjunto c
            ON c.id = m.id_conjunto
        JOIN aneel.sig_indicador si
            ON si.id = m.id_sig_indicador
        JOIN aneel.limites l
            ON l.id_conjunto = c.id
           AND l.ano = :ano
        WHERE m.ano_indice = :ano
          AND m.num_periodo_indice = :mes
          AND si.indicador_type IN ('DEC', 'FEC')
          AND (:nomeConjunto IS NULL OR :nomeConjunto = '' OR LOWER(c.dsc_conj_und_consumidoras) LIKE LOWER(CONCAT('%', :nomeConjunto, '%')))
          AND l.dec_lim IS NOT NULL
          AND l.fec_lim IS NOT NULL
        GROUP BY c.id, c.dsc_conj_und_consumidoras, l.dec_lim, l.fec_lim
        HAVING COUNT(DISTINCT si.indicador_type) = 2
        ORDER BY c.dsc_conj_und_consumidoras
        """, nativeQuery = true)
    List<CriticidadeMapaProjection> buscarCriticidadeParaMapa(
        @Param("mes") Long mes,
        @Param("ano") Long ano,
        @Param("nomeConjunto") String nomeConjunto
    );
}