package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.Usuario;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

@Service
public class UsuarioCadastroService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UsuarioCadastroRepository usuarioCadastroRepository;
    private final AppUserJpaRepository appUserRepository;
    private final TermsService termsService;
    private final UserStatusService userStatusService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioCadastroService(
            UsuarioCadastroRepository usuarioCadastroRepository,
            AppUserJpaRepository appUserRepository,
            TermsService termsService,
            UserStatusService userStatusService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioCadastroRepository = usuarioCadastroRepository;
        this.appUserRepository = appUserRepository;
        this.termsService = termsService;
        this.userStatusService = userStatusService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(UsuarioCadastroRequest request) {
        validarRequest(request);

        String emailNormalizado = normalizarEmail(request.getEmail());

        if (usuarioCadastroRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException("E-mail ja cadastrado.");
        }

        try {
            Usuario usuario = new Usuario();
            usuario.setNomeCompleto(request.getNomeCompleto().trim());
            usuario.setEmail(emailNormalizado);
            usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
            usuario.setTelefone(normalizarOpcional(request.getTelefone()));
            usuario.setStatus(StatusUsuario.PENDENTE);
            usuario.setDataCadastro(LocalDateTime.now());

            Usuario usuarioSalvo = usuarioCadastroRepository.save(usuario);

            if (request.getTermsIds() != null && !request.getTermsIds().isEmpty()) {
                AppUserEntity appUserSalvo = appUserRepository.findByEmailIgnoreCase(usuarioSalvo.getEmail())
                        .orElseThrow(() -> new IllegalStateException("Usuario cadastrado nao encontrado para registrar termos."));
                termsService.registrarTermosAceitos(request.getTermsIds(), appUserSalvo);
            }

            return usuarioSalvo;
        } catch (DataIntegrityViolationException ex) {
            throw new EmailJaCadastradoException("E-mail ja cadastrado.");
        }
    }

    @Transactional
    public void alterarStatusUsuario(UUID usuarioId, UUID adminId, StatusUsuario novoStatus, String motivo) {
        AppUserEntity usuario = appUserRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));
        AppUserEntity admin = appUserRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin nao encontrado."));

        boolean isAdmin = admin.getRoles() != null
                && admin.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        if (!isAdmin) {
            throw new PermissaoNegadaException("Apenas administradores podem alterar o status de usuarios.");
        }

        userStatusService.transitionFromPending(usuario, admin, novoStatus, motivo);
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
