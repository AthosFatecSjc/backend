package com.energia.backend.dto;

import java.util.UUID;

public record BackupUserPersonalData(
        UUID userId,
        String name,
        String email,
        String phone
) {
}
