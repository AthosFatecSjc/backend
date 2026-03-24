package com.energia.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserRoleResponseDTO {

    private UUID id;
    private UUID userId;
    private UUID roleId;
}