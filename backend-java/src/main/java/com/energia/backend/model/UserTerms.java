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

    // 🟢 quando aceitou
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    // 🌐 origem (WEB, APP, etc)
    @Column(name = "accepted_from", length = 45)
    private String acceptedFrom;

    // 🌐 IP de aceite
    @Column(name = "accepted_from_ip", length = 45)
    private String acceptedFromIp;

    // 🔒 status explícito (melhora muito a clareza)
    @Column(name = "accepted")
    private Boolean accepted;

    // 🔄 revogação
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}