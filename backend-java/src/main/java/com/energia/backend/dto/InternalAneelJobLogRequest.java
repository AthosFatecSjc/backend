package com.energia.backend.dto;

import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InternalAneelJobLogRequest {

    @NotNull(message = "O evento e obrigatorio")
    private LogEvent event;

    @NotNull(message = "O resultado e obrigatorio")
    private ResultType result;

    @NotBlank(message = "A descricao e obrigatoria")
    @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
    private String description;

    @Size(max = 4000, message = "Os metadados devem ter no maximo 4000 caracteres")
    private String metadata;

    @Size(max = 100, message = "A referencia alvo deve ter no maximo 100 caracteres")
    private String targetRef;
}
