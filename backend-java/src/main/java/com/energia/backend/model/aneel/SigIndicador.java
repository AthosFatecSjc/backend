package com.energia.backend.model.aneel;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "sig_indicador", schema = "aneel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SigIndicador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "sigIndicador")
	private List<Metricas> metrics;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "indicador_type", nullable = false, unique=true)
    private IndicadorType indicadorType;



    
}