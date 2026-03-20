package com.energia.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "terms", schema = "energia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_type_id")
    private TermTypeEntity termType;

    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "effectivity_start")
    private LocalDateTime effectivityStart;

    @Column(name = "effectivity_end")
    private LocalDateTime effectivityEnd;

    @Column(name = "mandatory", nullable = false)
    private Boolean mandatory = Boolean.FALSE;
}
