package com.energia.backend.model.term;

import java.util.UUID;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AcceptedTermModel {
    private UUID id;
    private Integer version;
}
