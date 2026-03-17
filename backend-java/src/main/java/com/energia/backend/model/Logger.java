package com.energia.backend.model;

import java.time.LocalDateTime;

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
    private LocalDateTime dateTime = LocalDateTime.now();
    private LogLevel level;
    private String conteudo;

}