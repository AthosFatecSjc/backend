package com.energia.backend.dto;

import java.util.ArrayList;

import com.energia.backend.model.StatusUsuario;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UsuarioCadastroResponse {
    private String mensagem;
    private String email;
    private ArrayList<StatusUsuario> status;
}