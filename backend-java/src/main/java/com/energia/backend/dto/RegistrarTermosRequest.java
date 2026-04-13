package com.energia.backend.dto;

import java.util.List;

public class RegistrarTermosRequest {
    private List<String> termsNames;

    public List<String> getTermsNames() {
        return termsNames;
    }

    public void setTermsNames(List<String> termsNames) {
        this.termsNames = termsNames;
    }
}
