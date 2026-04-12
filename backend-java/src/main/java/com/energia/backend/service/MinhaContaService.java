package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.AtualizarEmailRequest;
import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;

@Service
public class MinhaContaService {
    private final AppUserJpaRepository appUserRepository;
    private final UserStatusService userStatusService;

    public MinhaContaService(
            AppUserJpaRepository appUserRepository,
            UserStatusService userStatusService
    ) {
        this.appUserRepository = appUserRepository;
        this.userStatusService = userStatusService;
    }

    @Transactional(readOnly = true)
    public MinhaContaResponse consultar(UUID uidAutenticado) {
        AppUserEntity usuario = buscarUsuarioPorUid(uidAutenticado);
        return toResponse(usuario);
    }

    @Transactional
    public MinhaContaResponse atualizar(UUID uidAutenticado, MinhaContaUpdateRequest request) {
        validarUpdateRequest(request);

        AppUserEntity usuario = buscarUsuarioPorUid(uidAutenticado);

        if (request.getNomeCompleto() != null) {
            usuario.setName(normalizarNome(request.getNomeCompleto()));
        }

        if (request.getTelefone() != null) {
            usuario.setPhone(normalizarTelefone(request.getTelefone()));
        }

        AppUserEntity atualizado = appUserRepository.save(usuario);
        return toResponse(atualizado);
    }

    @Transactional
    public MinhaContaResponse atualizarEmail(UUID adminId, UUID usuarioId, AtualizarEmailRequest request) {
        validarAtualizacaoEmailRequest(request);

        AppUserEntity admin = buscarUsuarioPorUid(adminId);
        validarAdministrador(admin);

        AppUserEntity usuario = buscarUsuarioPorUid(usuarioId);
        String novoEmail = normalizarEmail(request.getNovoEmail());

        if (novoEmail.equalsIgnoreCase(usuario.getEmail())) {
            throw new IllegalArgumentException("O novo e-mail deve ser diferente do atual.");
        }

        if (appUserRepository.existsByEmailIgnoreCase(novoEmail)) {
            throw new IllegalArgumentException("O novo e-mail informado ja esta em uso.");
        }

        usuario.setEmail(novoEmail);
        AppUserEntity atualizado = appUserRepository.save(usuario);
        return toResponse(atualizado);
    }

    private AppUserEntity buscarUsuarioPorUid(UUID uidAutenticado) {
        if (uidAutenticado == null) {
            throw new IllegalArgumentException("UID do usuario autenticado invalido.");
        }

        return appUserRepository.findById(uidAutenticado)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario autenticado nao encontrado."));
    }

    private MinhaContaResponse toResponse(AppUserEntity usuario) {
        StatusUsuario statusAtual = obterStatusAtual(usuario);
        LocalDateTime dataCadastro = obterDataCadastro(usuario);

        return new MinhaContaResponse(
                usuario.getName(),
                usuario.getEmail(),
                usuario.getPhone(),
                statusAtual,
                dataCadastro
        );
    }

    private StatusUsuario obterStatusAtual(AppUserEntity usuario) {
        return userStatusService.resolveCurrentStatus(usuario);
    }

    private LocalDateTime obterDataCadastro(AppUserEntity usuario) {
        // Usar a data de criação da entidade (quando foi registrado)
        return usuario.getCreatedAt() != null ? usuario.getCreatedAt() : LocalDateTime.now();
    }

    private void validarUpdateRequest(MinhaContaUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payload de atualizacao obrigatorio.");
        }

        if (request.getNomeCompleto() == null && request.getTelefone() == null) {
            throw new IllegalArgumentException("Informe ao menos nomeCompleto ou telefone para atualizar.");
        }

        if (request.getNomeCompleto() != null && normalizarNome(request.getNomeCompleto()) == null) {
            throw new IllegalArgumentException("Nome completo invalido.");
        }
    }

    private void validarAtualizacaoEmailRequest(AtualizarEmailRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payload de atualizacao de e-mail obrigatorio.");
        }
        if (request.getNovoEmail() == null || request.getNovoEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Novo e-mail obrigatorio.");
        }
        normalizarEmail(request.getNovoEmail());
    }

    private void validarAdministrador(AppUserEntity admin) {
        boolean isAdmin = admin.getRoles() != null
                && admin.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        if (!isAdmin) {
            throw new PermissaoNegadaException("Apenas administradores podem alterar o e-mail do usuario.");
        }
    }

    private String normalizarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return nome.trim();
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return null;
        }
        return telefone.trim();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-mail invalido.");
        }

        String normalizado = email.trim().toLowerCase();
        if (!normalizado.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Formato de e-mail invalido.");
        }
        return normalizado;
    }
}
