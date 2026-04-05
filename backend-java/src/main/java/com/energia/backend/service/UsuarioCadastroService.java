package com.energia.backend.service;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.Usuario;
import com.energia.backend.repository.UsuarioCadastroRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class UsuarioCadastroService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UsuarioCadastroRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioCadastroService(UsuarioCadastroRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(UsuarioCadastroRequest request) {
        validarRequest(request);

        String emailNormalizado = normalizarEmail(request.getEmail());
        if (repository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException("E-mail ja cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.getNomeCompleto().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        usuario.setTelefone(normalizarOpcional(request.getTelefone()));
        usuario.setStatus(StatusUsuario.PENDENTE);
        usuario.setDataCadastro(LocalDateTime.now());

        try {
            return repository.save(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailJaCadastradoException("E-mail ja cadastrado.");
        }
    }

    private void validarRequest(UsuarioCadastroRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payload de cadastro obrigatorio.");
        }
        if (isBlank(request.getNomeCompleto())) {
            throw new IllegalArgumentException("Nome completo e obrigatorio.");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("E-mail e obrigatorio.");
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("E-mail invalido.");
        }
        if (isBlank(request.getSenha())) {
            throw new IllegalArgumentException("Senha e obrigatoria.");
        }
        if (request.getSenha().trim().length() < 8) {
            throw new IllegalArgumentException("Senha deve ter no minimo 8 caracteres.");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizarOpcional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

