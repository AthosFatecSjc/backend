package com.energia.backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.AdminUsuarioResponse;
import com.energia.backend.dto.AlterarRoleUsuarioRequest;
import com.energia.backend.dto.AprovacaoRejeicaoUsuarioRequest;
import com.energia.backend.dto.AtualizarEmailRequest;
import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.service.AdminUsuarioService;
import com.energia.backend.service.MinhaContaService;
import com.energia.backend.service.UserRoleService;
import com.energia.backend.service.UsuarioCadastroService;

@RestController
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;
    private final UsuarioCadastroService usuarioCadastroService;
    private final UserRoleService userRoleService;
    private final MinhaContaService minhaContaService;

    public AdminUsuarioController(
            AdminUsuarioService adminUsuarioService,
            UsuarioCadastroService usuarioCadastroService,
            UserRoleService userRoleService,
            MinhaContaService minhaContaService
    ) {
        this.adminUsuarioService = adminUsuarioService;
        this.usuarioCadastroService = usuarioCadastroService;
        this.userRoleService = userRoleService;
        this.minhaContaService = minhaContaService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(adminUsuarioService.listarUsuarios());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> alterarStatusUsuario(
            @PathVariable("id") UUID usuarioId,
            @RequestBody AprovacaoRejeicaoUsuarioRequest request,
            Principal principal
    ) {
        UUID adminId = obterUid(principal);
        usuarioCadastroService.alterarStatusUsuario(usuarioId, adminId, request.getStatus(), request.getMotivo());
        return ResponseEntity.ok("Status do usuario atualizado com sucesso.");
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<String> alterarRoleUsuario(
            @PathVariable("id") UUID usuarioId,
            @RequestBody AlterarRoleUsuarioRequest request,
            Principal principal
    ) {
        UUID adminId = obterUid(principal);
        userRoleService.alterarRoleUsuario(usuarioId, adminId, request.getRoleName());
        return ResponseEntity.ok("Role do usuario atualizado com sucesso.");
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<MinhaContaResponse> atualizarEmailUsuario(
            @PathVariable("id") UUID usuarioId,
            @RequestBody AtualizarEmailRequest request,
            Principal principal
    ) {
        UUID adminId = obterUid(principal);
        MinhaContaResponse response = minhaContaService.atualizarEmail(adminId, usuarioId, request);
        return ResponseEntity.ok(response);
    }

    private UUID obterUid(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }

        try {
            return UUID.fromString(principal.getName().trim());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identificador do usuario autenticado invalido.");
        }
    }
}
