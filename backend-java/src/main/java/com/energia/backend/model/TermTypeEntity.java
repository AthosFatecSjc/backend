package com.energia.backend.model;

import com.energia.backend.model.TermsEntity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "term_type")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TermTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @OneToMany(mappedBy = "termType", fetch = FetchType.LAZY)
    private List<TermsEntity> terms;
}