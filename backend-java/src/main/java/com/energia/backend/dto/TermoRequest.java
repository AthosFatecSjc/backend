package com.energia.backend.dto;

import java.time.LocalDateTime;

public class TermoRequest {
    
    private String termTypeName;
    private String content;
    private LocalDateTime effectivityStartAt;

    public String getTermTypeName() {
        return termTypeName;
    }

    public void setTermTypeName(String termTypeName) {
        this.termTypeName = termTypeName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getEffectivityStartAt() {
        return effectivityStartAt;
    }

    public void setEffectivityStartAt(LocalDateTime effectivityStartAt) {
        this.effectivityStartAt = effectivityStartAt;
    }
}