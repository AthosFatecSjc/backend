package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoTermoResponse(
        UUID userTermId,
        UUID termId,
        String type,
        Integer version,
        boolean required,
        String action,
        LocalDateTime actionAt,
        String sourceIp
) {
}
