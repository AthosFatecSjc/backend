package com.energia.backend.service;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.Usuario;
import com.energia.backend.repository.UsuarioCadastroRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Pattern;

@Service
public class UsuarioCadastroService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;


    private final UsuarioCadastroRepository repository;
    private final AppUserJpaRepository appUserRepository;
    private final StatusJpaRepository statusRepository;
    private final UserStatusJpaRepository userStatusRepository;

        public UsuarioCadastroService(UsuarioCadastroRepository repository,
                      AppUserJpaRepository appUserRepository,
                      StatusJpaRepository statusRepository,
                      UserStatusJpaRepository userStatusRepository) {
        this.repository = repository;
        this.appUserRepository = appUserRepository;
        this.statusRepository = statusRepository;
        this.userStatusRepository = userStatusRepository;
        }

        @Transactional
        public void aprovarUsuario(java.util.UUID usuarioId, java.util.UUID adminId) {
        AppUserEntity usuario = appUserRepository.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        AppUserEntity admin = appUserRepository.findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado."));
        StatusEntity statusAprovado = statusRepository.findByNameIgnoreCase("APROVADO")
            .orElseThrow(() -> new IllegalArgumentException("Status APROVADO não encontrado."));
        UserStatusEntity userStatus = UserStatusEntity.builder()
            .user(usuario)
            .status(statusAprovado)
            .assignedBy(admin)
            .assignedAt(java.time.LocalDateTime.now())
            .build();
        userStatusRepository.save(userStatus);
        }

        @Transactional
        public void rejeitarUsuario(java.util.UUID usuarioId, java.util.UUID adminId, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da rejeição é obrigatório.");
        }
        AppUserEntity usuario = appUserRepository.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        AppUserEntity admin = appUserRepository.findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado."));
        StatusEntity statusRejeitado = statusRepository.findByNameIgnoreCase("REJEITADO")
            .orElseThrow(() -> new IllegalArgumentException("Status REJEITADO não encontrado."));
        UserStatusEntity userStatus = UserStatusEntity.builder()
            .user(usuario)
            .status(statusRejeitado)
            .assignedBy(admin)
            .assignedAt(java.time.LocalDateTime.now())
            .rationaleForRejection(motivo)
            .build();
        userStatusRepository.save(userStatus);
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
        usuario.setSenhaHash(gerarHashSeguro(request.getSenha()));
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

    private String gerarHashSeguro(String senha) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            return "PBKDF2$" + ITERATIONS + "$" + saltBase64 + "$" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Falha ao gerar hash de senha.", e);
        } finally {
            spec.clearPassword();
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
