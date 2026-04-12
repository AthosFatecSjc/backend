package com.energia.backend.dto;

import java.util.UUID;

public record TermosPendentesResponse(
        UUID termId,
        String type,
        Integer version,
        boolean required
) {
}
