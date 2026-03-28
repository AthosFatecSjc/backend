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
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.LogRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;

@Service
public class AnonimizacaoService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ANONIMIZADO_MARKER = "ANONIMIZADO";
    private static final String INATIVO_STATUS = "INATIVO";
    private static final String ANONIMIZADO_EMAIL_SUFFIX = "@anonimizado.local";

    private final AppUserJpaRepository appUserRepository;
    private final StatusJpaRepository statusRepository;
    private final UserStatusJpaRepository userStatusRepository;
    private final LogRepository logRepository;

    public AnonimizacaoService(
            AppUserJpaRepository appUserRepository,
            StatusJpaRepository statusRepository,
            UserStatusJpaRepository userStatusRepository,
            LogRepository logRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.statusRepository = statusRepository;
        this.userStatusRepository = userStatusRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public AnonimizarUsuarioResponse anonimizar(UUID adminId, AnonimizarUsuarioRequest request) {
        AppUserEntity admin = buscarUsuarioPorId(adminId);
        validarPermissaoAdmin(admin);

        AppUserEntity usuarioAnonimizar = buscarUsuarioPorId(request.usuarioId());
        validarJaAnonimizado(usuarioAnonimizar);

        // Anonimizar dados pessoais
        anonimizarDadosPessoais(usuarioAnonimizar);

        // Desativar usuário
        desativarUsuario(usuarioAnonimizar, admin);

        // Registrar log
        registrarLogAnonimizacao(admin, usuarioAnonimizar);

        return new AnonimizarUsuarioResponse(
                usuarioAnonimizar.getId(),
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

    private void validarPermissaoAdmin(AppUserEntity admin) {
        boolean isAdmin = admin.getRoles() != null &&
                admin.getRoles().stream()
                        .anyMatch(role -> ADMIN_ROLE.equalsIgnoreCase(role.getName()));

        if (!isAdmin) {
            throw new PermissaoNegadaException("Apenas administradores podem anonimizar usuarios.");
        }
    }

    private void validarJaAnonimizado(AppUserEntity usuario) {
        if (ANONIMIZADO_MARKER.equalsIgnoreCase(usuario.getName())) {
            throw new UsuarioJaAnonimizadoException("Usuario ja foi anonimizado anteriormente.");
        }
    }

    private void anonimizarDadosPessoais(AppUserEntity usuario) {
        // Anonimizar name
        usuario.setName(ANONIMIZADO_MARKER);

        // Anonimizar email - gerar um unico para manter constraint UNIQUE
        String novoEmail = gerarEmailAnonimizado(usuario.getId());
        usuario.setEmail(novoEmail);

        // Remover phone
        usuario.setPhone(null);

        // Persistir alteracoes
        appUserRepository.save(usuario);
    }

    private String gerarEmailAnonimizado(UUID userId) {
        return userId + ANONIMIZADO_EMAIL_SUFFIX;
    }

    private void desativarUsuario(AppUserEntity usuario, AppUserEntity admin) {
        StatusEntity statusInativo = statusRepository.findByNameIgnoreCase(INATIVO_STATUS)
                .orElseThrow(() -> new IllegalStateException("Status INATIVO nao encontrado no sistema."));

        UserStatusEntity novoStatus = UserStatusEntity.builder()
                .user(usuario)
                .status(statusInativo)
                .assignedBy(admin)
                .assignedAt(LocalDateTime.now())
                .build();

        userStatusRepository.save(novoStatus);
    }

    private void registrarLogAnonimizacao(AppUserEntity admin, AppUserEntity usuario) {
        SystemLog log = new SystemLog();
        log.setActorRef(admin.getId().toString());
        log.setTargetRef(usuario.getId().toString());
        log.setSourceType(SourceType.USER);
        log.setEvent(LogEvent.USER_ANONYMIZED);
        log.setResult(ResultType.SUCCESS);
        log.setLogCategory(LogCategory.AUDIT);
        log.setDescription("Usuario anonimizado com sucesso. Dados pessoais removidos.");
        log.setCreatedByModule("AnonimizacaoService");

        logRepository.save(log);
    }
}
