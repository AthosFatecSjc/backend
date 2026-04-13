package com.energia.backend.repository;

import com.energia.backend.model.user.AppUserModel;
import com.energia.backend.model.AppUserEntity;

public interface UsuarioCadastroRepository {
    boolean existsByEmail(String email);
    AppUserEntity save(AppUserModel user);
}
