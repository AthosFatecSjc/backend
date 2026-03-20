package com.energia.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "term_type", schema = "energia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
