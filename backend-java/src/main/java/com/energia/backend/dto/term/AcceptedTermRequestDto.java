package com.energia.backend.dto.term;

import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AcceptedTermRequestDto {
    private UUID id;
    private Integer version;
}
