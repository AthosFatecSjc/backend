package com.energia.backend.dto.error;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ErrorResponseDto {
    private String error;
    private String message;
}
