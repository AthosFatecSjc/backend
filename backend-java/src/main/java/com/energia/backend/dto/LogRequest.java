package com.energia.backend.dto;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogRequest {

    @Size(max = 100, message = "O identificador do ator deve ter no maximo 100 caracteres")
    private String actor;

    @NotNull(message = "O tipo de origem (sourceType) e obrigatorio")
    private SourceType sourceType;

    @NotNull(message = "O evento (event) e obrigatorio")
    private LogEvent event;

    @NotNull(message = "O resultado (result) e obrigatorio")
    private ResultType result;

    @NotNull(message = "A categoria do log (logCategory) e obrigatoria")
    private LogCategory logCategory;

    @NotBlank(message = "A descricao nao pode estar vazia")
    @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
    private String description;

    @Size(max = 4000, message = "Os metadados devem ter no maximo 4000 caracteres")
    private String metadata;

    @Size(max = 100, message = "A referencia do alvo deve ter no maximo 100 caracteres")
    private String targetRef;

    @NotBlank(message = "O modulo de origem deve ser identificado")
    @Size(max = 100, message = "O modulo de origem deve ter no maximo 100 caracteres")
    private String module;
}
