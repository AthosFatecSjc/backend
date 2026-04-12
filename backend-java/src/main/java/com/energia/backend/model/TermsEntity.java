package com.energia.backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "terms",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_terms_type_version", columnNames = {"term_type_id", "version"})
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TermsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // MANY TERMS → ONE TYPE
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "term_type_id", nullable = false)
    private TermTypeEntity termType;

    @Column(name = "version")
    private Integer version;
    
    @Column(name = "effectivity_start_at", nullable = false)
    private LocalDateTime effectivityStartAt;
   
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "terms", fetch = FetchType.LAZY)
    private List<UserTermsEntity> userAcceptances;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;



}