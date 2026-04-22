package com.energia.backend.dto;

import java.util.List;
import java.util.UUID;

public class RegistrarTermosRequest {
    private List<UUID> termsIds;

    public List<UUID> getTermsIds() {
        return termsIds;
    }

    public void setTermsIds(List<UUID> termsIds) {
        this.termsIds = termsIds;
    }
}
