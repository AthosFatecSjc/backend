package com.energia.backend.model.user;

import java.util.UUID;
import java.time.LocalDateTime;

import com.energia.backend.model.StatusUsuario;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class AppUserModel {
    private UUID id;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private StatusUsuario status;
    private LocalDateTime createdAt;
}
