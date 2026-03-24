package com.energia.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terms {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "term_type_id", nullable = false)
    private TermType termType;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "effectivity_start_at")
    private LocalDateTime effectivityStartAt;

    @Column(name = "effectivity_end_at")
    private LocalDateTime effectivityEndAt;

    @Column(columnDefinition = "TEXT")
    private String content;
}