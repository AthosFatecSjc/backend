package com.energia.backend.model.aneel;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "coleta", schema = "aneel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ColetaDados {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_key", nullable = false)
    private DataKey dataKey;

    @Column(name = "id_data", nullable = false)
    private Long idData;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Column(name = "data_geracao")
    private LocalDate dataGeracao;

    @Column(name = "link", nullable = false)
    private String link;

}
