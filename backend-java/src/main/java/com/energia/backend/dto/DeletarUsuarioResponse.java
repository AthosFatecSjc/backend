package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeletarUsuarioResponse(
        UUID usuarioId,
        String mensagem,
        LocalDateTime deletadoEm
) {}
