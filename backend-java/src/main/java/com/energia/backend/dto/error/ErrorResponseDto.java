package com.energia.backend.dto.error;

import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ErrorResponseDto {
    private String error;
    private String message;
}
