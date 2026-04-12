package com.energia.backend.dto;

import com.energia.backend.dto.term.AcceptedTermRequestDto;

import java.util.List;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UsuarioCadastroRequest {
    private String nomeCompleto;
    private String email;
    private String senha;
    private String telefone;
    private List<AcceptedTermRequestDto> terms; 
}
