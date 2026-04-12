package com.energia.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class UsuarioCadastroRequest {
    private String nomeCompleto;
    private String email;
    private String senha;
    private String telefone;
    private List<String> termsNames; 
}
