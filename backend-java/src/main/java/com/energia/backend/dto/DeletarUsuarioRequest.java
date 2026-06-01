package com.energia.backend.dto;

import java.util.UUID;

public record DeletarUsuarioRequest(
        UUID usuarioId
) {}
