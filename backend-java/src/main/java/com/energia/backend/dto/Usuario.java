package com.energia.backend.dto;

import java.time.LocalDateTime;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Usuario {
    private String nomeCompleto;
    private String email;
    private String senhaHash;
    private String telefone;
    private StatusUsuario status;
    private LocalDateTime dataCadastro;

    public AppUserEntity toEntity() {
        AppUserEntity entity = new AppUserEntity();
        entity.setName(this.nomeCompleto);
        entity.setEmail(this.email);
        entity.setPassword(this.senhaHash);
        entity.setPhone(this.telefone);
        entity.setStatuses(this.status);
        entity.setCreatedAt(this.dataCadastro);
        return entity;
    }
}
