package com.energia.backend.model.aneel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "limites", schema = "aneel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Limites {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_conjunto", nullable = false)
	private Conjunto conjunto;

	@Column(name = "ano", nullable = false)
    private Long ano;

	@Column(name = "dec_lim", nullable = false)
    private Double decLim;

	@Column(name = "fec_lim", nullable = false)
    private Double fecLim;

    
}