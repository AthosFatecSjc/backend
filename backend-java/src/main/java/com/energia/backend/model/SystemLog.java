package com.energia.backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "system_logs", schema = "energia")
public class SystemLog  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String actorRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogEvent event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultType result;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private String targetRef;

    private String createdByModule;
}