package com.energia.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserRoleRequestDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID roleId;
}