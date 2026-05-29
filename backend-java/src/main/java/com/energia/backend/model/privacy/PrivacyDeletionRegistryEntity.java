package com.energia.backend.model.privacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "privacy_deletion_registry",
        schema = "energia",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_privacy_deletion_registry_entity",
                        columnNames = {"entity_type", "entity_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivacyDeletionRegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private DeletedEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "strategy_version")
    private Integer strategyVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "restore_action", length = 20)
    private RestoreAction restoreAction;

    @Column(name = "retention_until")
    private LocalDateTime retentionUntil;

    @Column(name = "last_reconciled_at")
    private LocalDateTime lastReconciledAt;

    @Column(name = "last_reapplied_at")
    private LocalDateTime lastReappliedAt;

    @Column(name = "active")
    private boolean active;
}
