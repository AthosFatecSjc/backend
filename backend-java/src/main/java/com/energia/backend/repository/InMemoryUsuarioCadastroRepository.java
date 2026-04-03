package com.energia.backend.repository;

import com.energia.backend.dto.Usuario;
import org.springframework.stereotype.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryUsuarioCadastroRepository implements UsuarioCadastroRepository {
    private final ConcurrentMap<String, Usuario> usuariosPorEmail = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmail(String email) {
        return usuariosPorEmail.containsKey(normalizarEmail(email));
    }

    @Override
    public Usuario save(Usuario usuario) {
        usuariosPorEmail.put(normalizarEmail(usuario.getEmail()), usuario);
        return usuario;
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
