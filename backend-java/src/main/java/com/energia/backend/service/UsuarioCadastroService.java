package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.JpaUsuarioCadastroRepository;
import com.energia.backend.repository.RoleJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

@Service
public class UsuarioCadastroService {
    private final UsuarioCadastroRepository usuarioCadastroRepository;
    private final AppUserJpaRepository appUserRepository;
    private final TermsUserService termsUserService;
    private final StatusJpaRepository statusRepository;
    private final UserStatusService userStatusService;
    private final PasswordEncoder passwordEncoder;
    private final JpaUsuarioCadastroRepository jpaUsuarioCadastroRepository;
    private final RoleJpaRepository roleRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    public UsuarioCadastroService(
            UsuarioCadastroRepository usuarioCadastroRepository,
            AppUserJpaRepository appUserRepository,
            TermsUserService termsUserService,
            StatusJpaRepository statusRepository,
            UserStatusJpaRepository userStatusRepository,
            TermsService termsService,
            UserStatusService userStatusService,
            PasswordEncoder passwordEncoder,
            JpaUsuarioCadastroRepository jpaUsuarioCadastroRepository,
            RoleJpaRepository roleRepository) {
        this.usuarioCadastroRepository = usuarioCadastroRepository;
        this.appUserRepository = appUserRepository;
        this.termsUserService = termsUserService;
        this.statusRepository = statusRepository;
        this.userStatusService = userStatusService;
        this.passwordEncoder = passwordEncoder;
        this.jpaUsuarioCadastroRepository = jpaUsuarioCadastroRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public AppUserEntity cadastrar(UsuarioCadastroRequest request) {
        validarRequest(request);

        String emailNormalizado = jpaUsuarioCadastroRepository.normalizarEmail(request.getEmail());

        if (usuarioCadastroRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException(emailNormalizado);
        }

        if (request.getTermsNames() == null || request.getTermsNames().isEmpty()) {
            throw new IllegalStateException("Usuario cadastrado nao aceitou nenhum termo");
        }

        AppUserEntity user = new AppUserEntity();
        user.setName(request.getNomeCompleto().trim());
        user.setEmail(emailNormalizado);
        user.setPassword(passwordEncoder.encode(request.getSenha()));
        user.setPhone(sanitizeOptional(request.getTelefone()));
        user.setCreatedAt(LocalDateTime.now());

        StatusEntity statusEntity = statusRepository
                .findByNameIgnoreCase(StatusUsuario.PENDENTE.name())
                .orElseThrow(() -> new RuntimeException("Status não encontrado"));

        UserStatusEntity userStatus = new UserStatusEntity();
        userStatus.setUser(user);
        userStatus.setStatus(statusEntity);
        userStatus.setAssignedAt(LocalDateTime.now());

        user.getStatuses().add(userStatus);

        RoleEntity role = roleRepository.findByNameIgnoreCase("USER")
                .orElseThrow(() -> new RuntimeException("Role não encontrada"));

        user.setRoles(List.of(role));

        AppUserEntity usuarioSalvo = appUserRepository.save(user);

        termsUserService.aprovarTermos(request.getTermsNames(), usuarioSalvo);
        return usuarioSalvo;

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

        if (!termsUserService.checkRequiredTerms(request.getTermsNames(), null, LocalDateTime.now())) {
            throw new IllegalArgumentException("Usuario deve aceitar todos os termos obrigatorios.");
        }
    }

    private String sanitizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
