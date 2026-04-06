package com.energia.backend.model.aneel;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(
    name = "metricas",
    schema = "aneel",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_metricas",
            columnNames = {
                "id_conjunto",
                "id_sig_indicador",
                "num_periodo_indice",
                "ano_indice"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Metricas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_conjunto", nullable = false)
	private Conjunto conjunto;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sig_indicador", nullable = false)
	private SigIndicador sigIndicador;

	@Column(name = "num_periodo_indice", nullable = false)
    private Long numPeriodoIndice;

	@Column(name = "ano_indice", nullable = false)
    private Long anoIndice;

	@Column(name = "data_geracao_conj_dados", nullable = false)
    private LocalDate dataGeracaoConjDados;

	@Column(name = "vlr_indice_enviado", nullable = false)
    private Double vlrIndiceEnviado;

    
}