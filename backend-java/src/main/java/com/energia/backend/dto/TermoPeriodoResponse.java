package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TermoPeriodoResponse(
    UUID termId,
    LocalDateTime dataInicio,
    LocalDateTime dataFinal
) {
}