package com.energia.backend.model.aneel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "limites",
    schema = "aneel",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_limites",
            columnNames = {"id_conjunto", "ano"}
        )
    }
)
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

	@Column(name = "dec_lim")
    private Double decLim;

	@Column(name = "fec_lim")
    private Double fecLim;

    
}