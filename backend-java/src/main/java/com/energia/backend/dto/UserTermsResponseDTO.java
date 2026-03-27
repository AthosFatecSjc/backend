package com.energia.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserTermsResponseDTO {

    private UUID id;

    private UUID userId;

    private UUID termsId;

    private LocalDateTime acceptedAt;

    private String acceptedFrom;

    private String acceptedFromIp; // 🌐 novo

    private Boolean accepted; // 🔒 opcional, mas útil

    private LocalDateTime revokedAt;
}