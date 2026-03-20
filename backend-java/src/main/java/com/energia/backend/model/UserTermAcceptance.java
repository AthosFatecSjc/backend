package com.energia.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_terms", schema = "energia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTermAcceptance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private TermEntity term;

    @Column(name = "accepted", nullable = false)
    private Boolean accepted;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
}