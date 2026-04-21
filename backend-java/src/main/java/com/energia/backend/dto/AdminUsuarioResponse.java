package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUsuarioResponse(
        UUID id,
        String nomeCompleto,
        String email,
        String telefone,
        String status,
        String role,
        LocalDateTime dataCadastro
) {
}
