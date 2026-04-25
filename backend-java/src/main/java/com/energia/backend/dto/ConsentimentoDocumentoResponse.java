package com.energia.backend.dto;

import java.util.UUID;

public class ConsentimentoDocumentoResponse {
    private final UUID documentId;
    private final String type;
    private final String content;
    private final boolean required;
    private final Integer clause;

    public ConsentimentoDocumentoResponse(
            UUID documentId,
            String type,
            String content,
            boolean required,
            Integer clause
    ) {
        this.documentId = documentId;
        this.type = type;
        this.content = content;
        this.required = required;
        this.clause = clause;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getType() {
        return type;
    }

    
    public String getContent() {
        return content;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public Integer getClause() {
        return clause;
    }

}
