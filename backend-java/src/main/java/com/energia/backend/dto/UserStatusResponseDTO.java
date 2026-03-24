package com.energia.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserStatusResponseDTO {

    private UUID id;
    private UUID statusId;
    private UUID userId;
    private UUID assignedByUserId;
    private LocalDateTime assignedAt;
    private String rationaleForRejection;
}