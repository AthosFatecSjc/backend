package com.energia.backend.dto;

import java.util.List;

public class ResolverPendenciasTermosLoginRequest {
    private String email;
    private String senha;
    private List<String> requiredTermsNames;
    private List<String> optionalAcceptedTermsNames;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<String> getRequiredTermsNames() {
        return requiredTermsNames;
    }

    public void setRequiredTermsNames(List<String> requiredTermsNames) {
        this.requiredTermsNames = requiredTermsNames;
    }

    public List<String> getOptionalAcceptedTermsNames() {
        return optionalAcceptedTermsNames;
    }

    public void setOptionalAcceptedTermsNames(List<String> optionalAcceptedTermsNames) {
        this.optionalAcceptedTermsNames = optionalAcceptedTermsNames;
    }
}
