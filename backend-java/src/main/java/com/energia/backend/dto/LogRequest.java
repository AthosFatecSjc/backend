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

    private String actor;

    @NotNull(message = "O tipo de origem (sourceType) é obrigatório")
    private SourceType sourceType;

    @NotNull(message = "O evento (event) é obrigatório")
    private LogEvent event;

    @NotNull(message = "O resultado (result) é obrigatório")
    private ResultType result;

    @NotNull(message = "A categoria do log (logCategory) é obrigatório")
    private LogCategory logCategory;

    @NotBlank(message = "A descrição não pode estar vazia")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String description;

    private String metadata;

    private String targetRef;

    @NotBlank(message = "O módulo de origem deve ser identificado")
    private String module;
}