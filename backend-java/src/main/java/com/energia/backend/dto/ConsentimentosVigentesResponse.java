package com.energia.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConsentimentosVigentesResponse {
    private final List<ConsentimentoDocumentoResponse> terms;
    private final List<ConsentimentoDocumentoResponse> privacy;
    private final List<ConsentimentoDocumentoResponse> marketing;
}
