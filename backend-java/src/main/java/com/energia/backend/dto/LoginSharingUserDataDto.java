package com.energia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSharingUserDataDto {
    
    private String email;
    private String nomeCompleto;
    private String telefone;
    private String status;
    private String message;
}
