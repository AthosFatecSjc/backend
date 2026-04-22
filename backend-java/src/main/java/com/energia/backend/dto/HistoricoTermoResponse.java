package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoTermoResponse(
        UUID userTermId,
        UUID termId,
        String typeName,
        boolean required,
        String action,
        LocalDateTime actionAt
) {
}
