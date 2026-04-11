package com.energia.backend.dto;

import java.util.List;
import java.util.UUID;

public class ResolverPendenciasTermosLoginRequest {
    private String email;
    private String senha;
    private List<UUID> requiredTermsIds;
    private List<UUID> optionalAcceptedTermsIds;

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

    public List<UUID> getRequiredTermsIds() {
        return requiredTermsIds;
    }

    public void setRequiredTermsIds(List<UUID> requiredTermsIds) {
        this.requiredTermsIds = requiredTermsIds;
    }

    public List<UUID> getOptionalAcceptedTermsIds() {
        return optionalAcceptedTermsIds;
    }

    public void setOptionalAcceptedTermsIds(List<UUID> optionalAcceptedTermsIds) {
        this.optionalAcceptedTermsIds = optionalAcceptedTermsIds;
    }
}
