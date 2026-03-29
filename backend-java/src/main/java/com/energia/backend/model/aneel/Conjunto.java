package com.energia.backend.model.aneel;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "conjunto", schema = "aneel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Conjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ide_conj_und_consumidoras", unique = true, nullable = false)
    private Long ideConjUndConsumidoras;

    @Column(name = "dsc_conj_und_consumidoras", nullable = false)
    private String dscConjUndConsumidoras;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_distribuidora", nullable = false)
	private Distribuidora distribuidora;

	@OneToMany(mappedBy = "conjunto")
	private List<Limites> limites;

    @Column(columnDefinition = "geometry(MultiPolygon, 4674)")
    private Geometry geometry;
}