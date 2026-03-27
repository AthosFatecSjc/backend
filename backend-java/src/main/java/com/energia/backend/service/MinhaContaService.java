package com.energia.backend.service;

import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MinhaContaService {
    private final AppUserJpaRepository appUserRepository;
    private final UserStatusJpaRepository userStatusRepository;

    public MinhaContaService(AppUserJpaRepository appUserRepository, UserStatusJpaRepository userStatusRepository) {
        this.appUserRepository = appUserRepository;
        this.userStatusRepository = userStatusRepository;
    }

    @Transactional(readOnly = true)
    public MinhaContaResponse consultar(String emailAutenticado) {
        AppUserEntity usuario = buscarUsuarioPorEmail(emailAutenticado);
        return toResponse(usuario);
    }

    @Transactional
    public MinhaContaResponse atualizar(String emailAutenticado, MinhaContaUpdateRequest request) {
        validarUpdateRequest(request);

        AppUserEntity usuario = buscarUsuarioPorEmail(emailAutenticado);

        if (request.getNomeCompleto() != null) {
            usuario.setName(normalizarNome(request.getNomeCompleto()));
        }

        if (request.getTelefone() != null) {
            usuario.setPhone(normalizarTelefone(request.getTelefone()));
        }

        AppUserEntity atualizado = appUserRepository.save(usuario);
        return toResponse(atualizado);
    }

    private AppUserEntity buscarUsuarioPorEmail(String emailAutenticado) {
        String emailNormalizado = normalizarEmail(emailAutenticado);
        if (emailNormalizado == null) {
            throw new IllegalArgumentException("Usuario autenticado invalido.");
        }

        return appUserRepository.findByEmailIgnoreCase(emailNormalizado)
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
        return userStatusRepository.findFirstByUserOrderByAssignedAtDesc(usuario)
                .map(UserStatusEntity::getStatus)
                .map(statusEntity -> toStatusUsuario(statusEntity.getName()))
                .orElse(null);
    }

    private LocalDateTime obterDataCadastro(AppUserEntity usuario) {
        return userStatusRepository.findFirstByUserOrderByAssignedAtAsc(usuario)
                .map(UserStatusEntity::getAssignedAt)
                .orElse(null);
    }

    private StatusUsuario toStatusUsuario(String value) {
        if (value == null) {
            return null;
        }
        try {
            return StatusUsuario.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
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

    private String normalizarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim().toLowerCase();
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
}
