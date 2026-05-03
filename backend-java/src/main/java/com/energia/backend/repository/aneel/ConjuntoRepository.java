package com.energia.backend.repository.aneel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.model.aneel.Conjunto;
import com.energia.backend.repository.aneel.projection.MapaCalorConjuntoProjection;

public interface ConjuntoRepository extends JpaRepository<Conjunto, Long> {
    Optional<Conjunto> findByIdeConjUndConsumidoras(Long ide);

    @Modifying
    @Transactional
    @Query(value = """
                UPDATE aneel.conjunto
                SET geometry = ST_SetSRID(ST_GeomFromGeoJSON(:geojson), 4674)
                WHERE ide_conj_und_consumidoras = :codId
            """, nativeQuery = true)
    void atualizarGeometria(Long codId, String geojson);

    @Query(value = """
            WITH metricas_rankeadas AS (
                SELECT
                    m.id_conjunto,
                    si.indicador_type,
                    m.ano_indice,
                    m.num_periodo_indice,
                    m.vlr_indice_enviado,
                    ROW_NUMBER() OVER (
                        PARTITION BY m.id_conjunto, si.indicador_type
                        ORDER BY m.ano_indice DESC, m.num_periodo_indice DESC, m.id DESC
                    ) AS rn
                FROM aneel.metricas m
                JOIN aneel.sig_indicador si ON si.id = m.id_sig_indicador
                WHERE (:ano IS NULL OR m.ano_indice = :ano)
                  AND (:mes IS NULL OR m.num_periodo_indice = :mes)
            ),
            metricas_atuais AS (
                SELECT
                    id_conjunto,
                    indicador_type,
                    ano_indice,
                    num_periodo_indice,
                    vlr_indice_enviado
                FROM metricas_rankeadas
                WHERE rn = 1
            )
            SELECT
                c.id AS conjuntoId,
                c.ide_conj_und_consumidoras AS ideConjUndConsumidoras,
                c.dsc_conj_und_consumidoras AS dscConjUndConsumidoras,
                COALESCE(d.razao_social, d.sig_agente) AS razaoSocial,
                d.uf AS uf,
                COALESCE(dec.ano_indice, fec.ano_indice) AS anoReferencia,
                COALESCE(dec.num_periodo_indice, fec.num_periodo_indice) AS periodoReferencia,
                dec.vlr_indice_enviado AS decValor,
                fec.vlr_indice_enviado AS fecValor,
                lim.dec_lim AS decLim,
                lim.fec_lim AS fecLim,
                p.perdas_nao_tec AS perdasNaoTec,
                p.custo_perdas_nao_tec AS custoPerdasNaoTec,
                ST_AsGeoJSON(ST_Transform(c.geometry, 4326)) AS geometryGeojson
            FROM aneel.conjunto c
            JOIN aneel.distribuidora d ON d.id = c.id_distribuidora
            LEFT JOIN metricas_atuais dec
                ON dec.id_conjunto = c.id
                AND dec.indicador_type = 'DEC'
            LEFT JOIN metricas_atuais fec
                ON fec.id_conjunto = c.id
                AND fec.indicador_type = 'FEC'
            LEFT JOIN aneel.limites lim
                ON lim.id_conjunto = c.id
                AND lim.ano = COALESCE(dec.ano_indice, fec.ano_indice)
            LEFT JOIN aneel.perdas p
                ON p.id_distribuidora = d.id
                AND p.ano = COALESCE(dec.ano_indice, fec.ano_indice)
            WHERE c.geometry IS NOT NULL
            ORDER BY d.uf, razaoSocial, c.dsc_conj_und_consumidoras
            """, nativeQuery = true)
    List<MapaCalorConjuntoProjection> buscarDadosMapaCalor(@Param("ano") Long ano, @Param("mes") Long mes);

    @Query(value = """
            SELECT DISTINCT m.ano_indice
            FROM aneel.metricas m
            ORDER BY m.ano_indice DESC
            """, nativeQuery = true)
    List<Long> listarAnosDisponiveisMapaCalor();

    @Query(value = """
            SELECT DISTINCT m.num_periodo_indice
            FROM aneel.metricas m
            WHERE (:ano IS NULL OR m.ano_indice = :ano)
            ORDER BY m.num_periodo_indice DESC
            """, nativeQuery = true)
    List<Long> listarMesesDisponiveisMapaCalor(@Param("ano") Long ano);
}
