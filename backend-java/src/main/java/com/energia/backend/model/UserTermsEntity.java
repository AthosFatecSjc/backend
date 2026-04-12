package com.energia.backend.model;

import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.AppUserEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Table(name = "user_terms")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserTermsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // MANY → ONE (User)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    // MANY → ONE (Terms)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private TermsEntity terms;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "accepted_from_ip", nullable = false, length = 45)
    private String acceptedFromIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private UserTermsEventType eventType;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

}
