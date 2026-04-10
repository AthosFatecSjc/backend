package com.energia.backend.dto;

import java.util.List;
import java.util.UUID;

public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private UUID userId;
    private String email;
    private String nome;
    private boolean isAdmin;
    private String status;
    private List<String> roles;
    private boolean mustChangePasswordOnFirstLogin;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, UUID userId, String email, String nome,
                         boolean isAdmin, String status, List<String> roles,
                         boolean mustChangePasswordOnFirstLogin) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.email = email;
        this.nome = nome;
        this.isAdmin = isAdmin;
        this.status = status;
        this.roles = roles;
        this.mustChangePasswordOnFirstLogin = mustChangePasswordOnFirstLogin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isMustChangePasswordOnFirstLogin() {
        return mustChangePasswordOnFirstLogin;
    }

    public void setMustChangePasswordOnFirstLogin(boolean mustChangePasswordOnFirstLogin) {
        this.mustChangePasswordOnFirstLogin = mustChangePasswordOnFirstLogin;
    }
}
