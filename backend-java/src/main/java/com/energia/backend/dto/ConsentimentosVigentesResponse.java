package com.energia.backend.dto;

public class ConsentimentosVigentesResponse {
    private final ConsentimentoDocumentoResponse terms;
    private final ConsentimentoDocumentoResponse privacy;
    private final ConsentimentoDocumentoResponse marketing;

    public ConsentimentosVigentesResponse(
            ConsentimentoDocumentoResponse terms,
            ConsentimentoDocumentoResponse privacy,
            ConsentimentoDocumentoResponse marketing
    ) {
        this.terms = terms;
        this.privacy = privacy;
        this.marketing = marketing;
    }

    public ConsentimentoDocumentoResponse getTerms() {
        return terms;
    }

    public ConsentimentoDocumentoResponse getPrivacy() {
        return privacy;
    }

    public ConsentimentoDocumentoResponse getMarketing() {
        return marketing;
    }
}
