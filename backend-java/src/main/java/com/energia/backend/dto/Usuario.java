package com.energia.backend.dto;

import java.time.LocalDateTime;

import com.energia.backend.model.StatusUsuario;

import lombok.Data;

@Data
public class Usuario {
    private String nomeCompleto;
    private String email;
    private String senhaHash;
    private String telefone;
    private StatusUsuario status;
    private LocalDateTime dataCadastro;
}
