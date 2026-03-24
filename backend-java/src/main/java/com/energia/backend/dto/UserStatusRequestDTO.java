package com.energia.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserStatusRequestDTO {

    @NotNull
    private UUID statusId;

    @NotNull
    private UUID userId;

    private UUID assignedByUserId;

    private String rationaleForRejection;
}