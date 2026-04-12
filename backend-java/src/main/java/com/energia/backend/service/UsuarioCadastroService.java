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
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

@Service
public class UsuarioCadastroService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final UsuarioCadastroRepository usuarioCadastroRepository;
    private final AppUserJpaRepository appUserRepository;
    private final TermsUserService termsUserService;
    private final StatusJpaRepository statusRepository;
    private final UserStatusJpaRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioCadastroService(
            UsuarioCadastroRepository usuarioCadastroRepository,
            AppUserJpaRepository appUserRepository,
            TermsUserService termsUserService,
            StatusJpaRepository statusRepository,
            UserStatusJpaRepository userStatusRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioCadastroRepository = usuarioCadastroRepository;
        this.appUserRepository = appUserRepository;
        this.termsUserService = termsUserService;
        this.statusRepository = statusRepository;
        this.userStatusRepository = userStatusRepository;
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
            if (request.getTermsNames() == null || request.getTermsNames().isEmpty())
                {throw new IllegalStateException("Usuario cadastrado nao aceitou nenhum termo");}
            else {
                Usuario usuario = new Usuario();
                usuario.setNomeCompleto(request.getNomeCompleto().trim());
                usuario.setEmail(emailNormalizado);
                usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
                usuario.setTelefone(normalizarOpcional(request.getTelefone()));
                usuario.setStatus(StatusUsuario.PENDENTE);
                usuario.setDataCadastro(LocalDateTime.now());

                Usuario usuarioSalvo = usuarioCadastroRepository.save(usuario);
                
                termsUserService.aprovarTermos(request.getTermsNames(), usuarioSalvo.toEntity());
                return usuarioSalvo;

            }
            
        } catch (DataIntegrityViolationException ex) {
            throw new EmailJaCadastradoException("E-mail ja cadastrado.");
        }
    }

    @Transactional
    public void alterarStatusUsuario(UUID usuarioId, UUID adminId, StatusUsuario novoStatus, String motivo) {
        validarDependenciasStatus();

        if (novoStatus == null) {
            throw new IllegalArgumentException("Status desejado e obrigatorio.");
        }
        if (novoStatus != StatusUsuario.APROVADO && novoStatus != StatusUsuario.REJEITADO) {
            throw new IllegalArgumentException("Status deve ser APROVADO ou REJEITADO.");
        }
        if (novoStatus == StatusUsuario.REJEITADO && isBlank(motivo)) {
            throw new IllegalArgumentException("Motivo da rejeicao e obrigatorio.");
        }

        AppUserEntity usuario = appUserRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));
        AppUserEntity admin = appUserRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin nao encontrado."));

        boolean isAdmin = admin.getRoles() != null
                && admin.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        if (!isAdmin) {
            throw new PermissaoNegadaException("Apenas administradores podem alterar o status de usuarios.");
        }

        UserStatusEntity statusAtual = userStatusRepository.findFirstByUserOrderByAssignedAtDesc(usuario)
                .orElse(null);
        if (statusAtual == null
                || statusAtual.getStatus() == null
                || !"PENDENTE".equalsIgnoreCase(statusAtual.getStatus().getName())) {
            throw new IllegalStateException("So e permitido aprovar ou rejeitar usuarios com status PENDENTE.");
        }

        StatusEntity statusEntity = statusRepository.findByNameIgnoreCase(novoStatus.name())
                .orElseThrow(() -> new IllegalArgumentException("Status " + novoStatus + " nao encontrado."));

        UserStatusEntity novoUserStatus = UserStatusEntity.builder()
                .user(usuario)
                .status(statusEntity)
                .assignedBy(admin)
                .assignedAt(LocalDateTime.now())
                .rationaleForRejection(novoStatus == StatusUsuario.REJEITADO ? motivo.trim() : null)
                .build();

        userStatusRepository.save(novoUserStatus);
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

        if (!termsUserService.checkRequiredTerms(request.getTermsNames(), null, LocalDateTime.now())) {
            throw new IllegalArgumentException("Usuario deve aceitar todos os termos obrigatorios.");
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

    private void validarDependenciasStatus() {
        if (statusRepository == null || userStatusRepository == null || appUserRepository == null) {
            throw new IllegalStateException("DependÃªncias nÃ£o inicializadas para operaÃ§Ã£o de status.");
        }
    }
}
