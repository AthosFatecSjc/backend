package com.energia.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TermTypeResponseDTO {

    private UUID id;
    private String name;
}