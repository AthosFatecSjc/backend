package com.energia.backend.dto;

import java.util.List;

public class ConsentimentosVigentesResponse {
    private final List<ConsentimentoDocumentoResponse> terms;
    private final List<ConsentimentoDocumentoResponse> privacy;
    private final List<ConsentimentoDocumentoResponse> marketing;

    public ConsentimentosVigentesResponse(
            List<ConsentimentoDocumentoResponse> terms,
            List<ConsentimentoDocumentoResponse> privacy,
            List<ConsentimentoDocumentoResponse> marketing
    ) {
        this.terms = terms;
        this.privacy = privacy;
        this.marketing = marketing;
    }

    public List<ConsentimentoDocumentoResponse> getTerms() {
        return terms;
    }

    public List<ConsentimentoDocumentoResponse> getPrivacy() {
        return privacy;
    }

    public List<ConsentimentoDocumentoResponse> getMarketing() {
        return marketing;
    }
}
