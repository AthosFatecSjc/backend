package com.energia.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "term_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermType {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 50, nullable = false)
    private String name;
}