package com.energia.backend.dto;

import java.util.UUID;

public record AnonimizarUsuarioRequest(
        UUID usuarioId
) {}
