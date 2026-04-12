package com.energia.backend.repository;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.user.AppUserModel;
import com.energia.backend.dto.Usuario;
import org.springframework.stereotype.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryUsuarioCadastroRepository implements UsuarioCadastroRepository {
    private final ConcurrentMap<String, AppUserEntity> usuariosPorEmail = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmail(String email) {
        return usuariosPorEmail.containsKey(normalizarEmail(email));
    }

    @Override
    public AppUserEntity save(AppUserModel userModel)
    {
        AppUserEntity userEntity = AppUserEntity.builder()
                .name(userModel.getFullName())
                .email(userModel.getEmail())
                .password(userModel.getPassword())
                .phone(userModel.getPhone())
                .build();

        usuariosPorEmail.put(userModel.getEmail(), userEntity);

        return userEntity;
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
