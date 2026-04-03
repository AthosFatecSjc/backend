package com.energia.backend.model.aneel;

import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "distribuidora", schema = "aneel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Distribuidora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_id_dist", nullable = false, unique = true)
    private Long codigoIdDist;

	@OneToMany(mappedBy = "distribuidora")
	private List<Perdas> perdas;

	@OneToMany(mappedBy = "distribuidora")
	private List<Conjunto> conjuntos;

	@OneToMany(mappedBy = "distribuidora")
	private List<Subestacao> subestacoes;
  
    @Column(name = "sig_agente", nullable = false, unique = true)
    private String sigAgente;

    @Column(name = "num_cnpj", nullable = false, unique = true, length = 14)
    private String numCnpj;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "regiao", nullable = false)
    private Regiao regiao;

    @Column(name = "uf", length = 2, nullable = false)
    private String uf;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;
}