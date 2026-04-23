package com.energia.backend.dto;

import java.util.UUID;

public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private UUID userId;
    private String email;
    private String nome;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, UUID userId, String email, String nome) {
        this(accessToken, userId, email, nome, null);
    }

    public LoginResponse(String accessToken, UUID userId, String email, String nome, String role) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.email = email;
        this.nome = nome;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
