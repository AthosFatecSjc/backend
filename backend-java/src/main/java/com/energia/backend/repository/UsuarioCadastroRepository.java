package com.energia.backend.repository;

import com.energia.backend.dto.Usuario;

public interface UsuarioCadastroRepository {
    boolean existsByEmail(String email);
    Usuario save(Usuario usuario);
}
