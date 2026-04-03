package com.energia.backend.service;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.dto.Usuario;
import com.energia.backend.repository.UsuarioCadastroRepository;
import com.energia.backend.repository.UsuarioRepository;

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

    private final UsuarioRepository usuarioRepository;
    private final TermsService termsService;

    public UsuarioCadastroService(UsuarioRepository usuarioRepository,
                                TermsService termsService) {
        this.usuarioRepository = usuarioRepository;
        this.termsService = termsService;
    }

    @Transactional
    public Usuario cadastrar(UsuarioCadastroRequest request) {
        validarRequest(request);

        String emailNormalizado = normalizarEmail(request.getEmail());

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException("E-mail ja cadastrado.");
        }

        AppUserEntity appUser = new AppUserEntity();
        appUser.setName(request.getNomeCompleto().trim());
        appUser.setEmail(emailNormalizado);
        appUser.setPassword(gerarHashSeguro(request.getSenha()));
        appUser.setPhone(normalizarOpcional(request.getTelefone()));

        try {
            AppUserEntity appUserSalvo = usuarioRepository.save(appUser);

            if (request.getTermsIds() != null && !request.getTermsIds().isEmpty()) {
                termsService.registrarTermosAceitos(request.getTermsIds(), appUserSalvo);
            }

            Usuario usuario = new Usuario();
            usuario.setNomeCompleto(appUserSalvo.getName());
            usuario.setEmail(appUserSalvo.getEmail());
            usuario.setSenhaHash(appUserSalvo.getPassword());
            usuario.setTelefone(appUserSalvo.getPhone());
            usuario.setStatus(StatusUsuario.PENDENTE);
            usuario.setDataCadastro(LocalDateTime.now());

            return usuario;

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
        if (request.getTermsIds() == null || request.getTermsIds().isEmpty()) {
            throw new TermoNaoEncontradoException("Aceite dos termos obrigatorios e obrigatorio.");
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
