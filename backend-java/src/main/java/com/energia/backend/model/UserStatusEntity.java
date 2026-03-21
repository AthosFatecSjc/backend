package com.energia.backend.model;

import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.AppUserEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_status")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MANY → ONE (Status)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private StatusEntity status;

    // MANY → ONE (User)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    // MANY → ONE (Assigned by User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private AppUserEntity assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "rationale_for_rejection", nullable = true,  columnDefinition = "TEXT")
    private String rationaleForRejection;
}