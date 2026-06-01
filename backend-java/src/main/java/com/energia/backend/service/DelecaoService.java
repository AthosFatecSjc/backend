package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.DeletarUsuarioRequest;
import com.energia.backend.dto.DeletarUsuarioResponse;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.UsuarioJaDeletadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.DeletionStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.repository.AppUserJpaRepository;

@Service
public class DelecaoService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final AppUserJpaRepository appUserRepository;
    private final UserPrivacyDeletionService userPrivacyDeletionService;

    public DelecaoService(
            AppUserJpaRepository appUserRepository,
            UserPrivacyDeletionService userPrivacyDeletionService
    ) {
        this.appUserRepository = appUserRepository;
        this.userPrivacyDeletionService = userPrivacyDeletionService;
    }

    @Transactional
    public DeletarUsuarioResponse deletar(UUID actorId, DeletarUsuarioRequest request) {
        AppUserEntity actor = buscarUsuarioPorId(actorId);
        AppUserEntity usuario = buscarUsuarioPorId(request.usuarioId());
        validarJaDeletado(usuario);
        validarPermissao(actor, usuario);

        String reason = actorId.equals(usuario.getId())
                ? "Solicitacao de exclusao pelo proprio usuario"
                : "Delecao administrativa";

        userPrivacyDeletionService.deleteUser(usuario.getId(), actor.getId().toString(), reason);

        return new DeletarUsuarioResponse(
                usuario.getId(),
                "Usuario deletado com sucesso.",
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
            throw new PermissaoNegadaException("Apenas administradores ou o proprio usuario podem deletar.");
        }
    }

    private void validarJaDeletado(AppUserEntity usuario) {
        if (usuario.getDeletionStatus() == DeletionStatus.DELETED) {
            throw new UsuarioJaDeletadoException("Usuario ja foi deletado anteriormente.");
        }
    }
}

