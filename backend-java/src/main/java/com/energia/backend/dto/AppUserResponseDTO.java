package com.energia.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AppUserResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;
}