package com.energia.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TermsResponseDTO {

    private UUID id;
    private UUID termTypeId;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime effectivityStartAt;
    private LocalDateTime effectivityEndAt;
    private String content;
}