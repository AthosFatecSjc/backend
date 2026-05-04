package com.energia.backend.model;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private TermsEntity terms;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private UserTermsAction action;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;
}