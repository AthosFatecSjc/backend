package com.energia.backend.model.log;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "system_logs", schema = "energia")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actor_ref")
    private String actorRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    private LogEvent event;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private ResultType result;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_category", nullable = false)
    private LogCategory logCategory;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "target_ref")
    private String targetRef;

    @Column(name = "created_by_module")
    private String createdByModule;
}