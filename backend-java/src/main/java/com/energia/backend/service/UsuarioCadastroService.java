package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Comparator;

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
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.model.user.AppUserModel;
import com.energia.backend.model.user.UserRegistrationModel;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

@Service
public class UsuarioCadastroService {
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
    public AppUserModel cadastrar(UserRegistrationModel model)
    {
        UserRegistrationModel sanitizedModel = sanitizeUserRegistrationModel(model);

        if (usuarioCadastroRepository.existsByEmail(sanitizedModel.getUser().getEmail()))
            throw new EmailJaCadastradoException(sanitizedModel.getUser().getEmail());

        try
        {
            AppUserEntity savedAppUser = usuarioCadastroRepository.save(sanitizedModel.getUser());

            termsService.registrarTermosAceitos(sanitizedModel.getAcceptedTerms(), savedAppUser);

            return AppUserModel.builder()
                    .id(savedAppUser.getId())
                    .fullName(savedAppUser.getName())
                    .email(savedAppUser.getEmail())
                    .password(savedAppUser.getPassword())
                    .phone(savedAppUser.getPhone())
                    .status(
                        savedAppUser.getStatuses().stream()
                            .max(Comparator.comparing(UserStatusEntity::getAssignedAt))
                            .map(us -> StatusUsuario.valueOf(us.getStatus().getName()))
                            .orElse(null)
                    )
                    .createdAt(savedAppUser.getCreatedAt())
                    .build();
        }
        catch (DataIntegrityViolationException ex)
        {
            throw new EmailJaCadastradoException(sanitizedModel.getUser().getEmail());
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

    private UserRegistrationModel sanitizeUserRegistrationModel(UserRegistrationModel model)
    {
        //Sanitize email - trim and convert to lowercase
        model.getUser().setEmail(model.getUser().getEmail().trim().toLowerCase());

        //Sanitize full name - trim
        model.getUser().setFullName(model.getUser().getFullName().trim());

        //Generate password hash
        model.getUser().setPassword(passwordEncoder.encode(model.getUser().getPassword()));

        //Sanitize phone number (optional)
        model.getUser().setPhone(sanitizeOptional(model.getUser().getPhone()));

        return model;
    }

    private String sanitizeOptional(String value)
    {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
