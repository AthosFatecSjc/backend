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
public class Logger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actorRef;
    private Boolean isAuditavel;
    @CreationTimestamp
    private LocalDateTime dateTime;
    @Enumerated(EnumType.STRING)
    private LogLevel level;
    private String conteudo;

}