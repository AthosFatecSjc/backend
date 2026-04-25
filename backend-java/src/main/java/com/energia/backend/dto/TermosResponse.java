package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TermosResponse(
        UUID termId,
        String typeName,
        Boolean required,
        String content, 
        Integer clause,
        LocalDateTime effectivityStartAt,
        LocalDateTime effectivityEndAt
) {
}
