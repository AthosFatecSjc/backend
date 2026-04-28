package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.AnonimizarUsuarioRequest;
import com.energia.backend.dto.AnonimizarUsuarioResponse;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.UsuarioJaAnonimizadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.AnonymizationStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.repository.AppUserJpaRepository;

@Service
public class AnonimizacaoService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final AppUserJpaRepository appUserRepository;
    private final UserPrivacyAnonymizationService userPrivacyAnonymizationService;

    public AnonimizacaoService(
            AppUserJpaRepository appUserRepository,
            UserPrivacyAnonymizationService userPrivacyAnonymizationService
    ) {
        this.appUserRepository = appUserRepository;
        this.userPrivacyAnonymizationService = userPrivacyAnonymizationService;
    }

    @Transactional
    public AnonimizarUsuarioResponse anonimizar(UUID actorId, AnonimizarUsuarioRequest request) {
        AppUserEntity actor = buscarUsuarioPorId(actorId);
        AppUserEntity usuario = buscarUsuarioPorId(request.usuarioId());
        validarJaAnonimizado(usuario);
        validarPermissao(actor, usuario);

        String reason = actorId.equals(usuario.getId())
                ? "Solicitacao de exclusao pelo proprio usuario"
                : "Anonimizacao administrativa";

        userPrivacyAnonymizationService.anonymizeUser(usuario.getId(), actor.getId().toString(), reason);

        return new AnonimizarUsuarioResponse(
                usuario.getId(),
                "Usuario anonimizado com sucesso.",
                LocalDateTime.now()
        );
    }

    private AppUserEntity buscarUsuarioPorId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuario invalido.");
        }

        return appUserRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario nao encontrado."));
    }

    private void validarPermissao(AppUserEntity actor, AppUserEntity target) {
        boolean isAdmin = actor.getRoles() != null && actor.getRoles().stream()
                .anyMatch(role -> ADMIN_ROLE.equalsIgnoreCase(role.getName()));

        boolean isSelf = actor.getId() != null && actor.getId().equals(target.getId());

        if (!isAdmin && !isSelf) {
            throw new PermissaoNegadaException("Apenas administradores ou o proprio usuario podem anonimizar.");
        }
    }

    private void validarJaAnonimizado(AppUserEntity usuario) {
        if (usuario.getAnonymizationStatus() == AnonymizationStatus.ANONYMIZED) {
            throw new UsuarioJaAnonimizadoException("Usuario ja foi anonimizado anteriormente.");
        }
    }
}

