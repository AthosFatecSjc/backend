package com.energia.backend.dto;

import java.util.UUID;

public record TermosResponse(
        UUID termId,
        String typeName,
        boolean required
) {
}
