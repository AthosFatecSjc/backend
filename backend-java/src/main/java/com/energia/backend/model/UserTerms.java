package com.energia.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTerms {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "accepted_from", length = 45)
    private String acceptedFrom;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}