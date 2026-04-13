package com.energia.backend.model.aneel;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "perdas", schema = "aneel",  uniqueConstraints = {
    @UniqueConstraint(name = "uk_perdas_dist_ano", columnNames = {"id_distribuidora", "ano"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Perdas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@ManyToOne
    @JoinColumn(name = "id_distribuidora", nullable = false)
    private Distribuidora distribuidora;

	@Column(name = "data_processo", nullable = false)
    private LocalDate dataProcesso;

    @Column(name = "ano", nullable = false)
    private Long ano;

	@Column(name = "perdas_nao_tec")
    private Double perdasNaoTec;

	@Column(name = "custo_perdas_nao_tec")
    private Double custoPerdasNaoTec;

    @PrePersist
    @PreUpdate
    public void preencherAno() {
        if (dataProcesso != null) {
            this.ano = (long) dataProcesso.getYear();
        }
    }
	
    
}
