package com.energia.backend.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConsentimentoDocumentoResponse {
    private final UUID documentId;
    private final String type;
    private final String content;
    private final boolean required;
    private final Integer clause;
}