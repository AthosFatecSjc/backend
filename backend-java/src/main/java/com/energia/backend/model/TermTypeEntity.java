package com.energia.backend.model;

import com.energia.backend.model.TermsEntity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "term_type")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TermTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @OneToMany(mappedBy = "termType", fetch = FetchType.LAZY)
    private List<TermsEntity> terms;
}