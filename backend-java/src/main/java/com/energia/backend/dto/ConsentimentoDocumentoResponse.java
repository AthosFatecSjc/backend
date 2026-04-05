package com.energia.backend.dto;

import java.util.UUID;

public class ConsentimentoDocumentoResponse {
    private final UUID documentId;
    private final String type;
    private final Integer version;
    private final String content;
    private final boolean required;

    public ConsentimentoDocumentoResponse(
            UUID documentId,
            String type,
            Integer version,
            String content,
            boolean required
    ) {
        this.documentId = documentId;
        this.type = type;
        this.version = version;
        this.content = content;
        this.required = required;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getType() {
        return type;
    }

    public Integer getVersion() {
        return version;
    }

    public String getContent() {
        return content;
    }

    public boolean isRequired() {
        return required;
    }
}
