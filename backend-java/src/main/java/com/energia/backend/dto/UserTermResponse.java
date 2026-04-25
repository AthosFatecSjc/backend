package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserTermResponse(
        UUID userTermId,
        UUID termId,
        String typeName,
        Boolean required,
        String action,
        LocalDateTime actionAt
) {
}
