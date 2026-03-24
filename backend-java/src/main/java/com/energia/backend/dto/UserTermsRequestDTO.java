package com.energia.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserTermsRequestDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID termsId;

    @NotNull
    private Boolean accepted;

    private String acceptedFrom;
}