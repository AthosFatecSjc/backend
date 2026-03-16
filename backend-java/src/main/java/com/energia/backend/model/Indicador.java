package com.energia.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "indicadores", schema = "energia")
public class Indicador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "concessionaria_id")
    private Concessionaria concessionaria;

    private Integer ano;
    private Integer mes;
    private Double decAnual;
    private Double fecAnual;
    private LocalDateTime dataAtualizacao;

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Concessionaria getConcessionaria() { return concessionaria; }
    public void setConcessionaria(Concessionaria concessionaria) { this.concessionaria = concessionaria; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }
    public Double getDecAnual() { return decAnual; }
    public void setDecAnual(Double decAnual) { this.decAnual = decAnual; }
    public Double getFecAnual() { return fecAnual; }
    public void setFecAnual(Double fecAnual) { this.fecAnual = fecAnual; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
