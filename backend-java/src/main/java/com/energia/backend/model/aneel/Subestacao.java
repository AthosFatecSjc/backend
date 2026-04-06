package com.energia.backend.model.aneel;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "subestacao", schema = "aneel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Subestacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_id_sub", nullable = false, unique = true)
    private String codIdSub;

    @Column(name = "name_sub", nullable = false)
    private String nameSub;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_distribuidora", nullable = false)
	private Distribuidora distribuidora;

    @Column(columnDefinition = "geometry(MultiPolygon, 4674)")
    private Geometry geometry;
}