package com.energia.backend.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.AnonimizarUsuarioRequest;
import com.energia.backend.dto.AnonimizarUsuarioResponse;
import com.energia.backend.dto.AprovacaoRejeicaoUsuarioRequest;
import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.dto.Usuario;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.service.AnonimizacaoService;
import com.energia.backend.service.MinhaContaService;
import com.energia.backend.service.UsuarioCadastroService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioCadastroService cadastroService;
    private final MinhaContaService minhaContaService;
    private final AnonimizacaoService anonimizacaoService;

    public UsuarioController(
            UsuarioCadastroService cadastroService,
            MinhaContaService minhaContaService,
            AnonimizacaoService anonimizacaoService
    ) {
        this.cadastroService = cadastroService;
        this.minhaContaService = minhaContaService;
        this.anonimizacaoService = anonimizacaoService;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> alterarStatusUsuario(
            @PathVariable("id") UUID usuarioId,
            @RequestBody AprovacaoRejeicaoUsuarioRequest request,
            Principal principal
    ) {
        UUID adminId = obterUidDoUsuarioAutenticado(principal);
        cadastroService.alterarStatusUsuario(usuarioId, adminId, request.getStatus(), request.getMotivo());
        return ResponseEntity.ok("Status do usuario atualizado com sucesso.");
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioCadastroResponse> cadastrar(@RequestBody UsuarioCadastroRequest request) {
        Usuario usuario = cadastroService.cadastrar(request);

        UsuarioCadastroResponse response = new UsuarioCadastroResponse(
                "Cadastro realizado com sucesso. Aguardando aprovacao do administrador.",
                usuario.getEmail(),
                usuario.getStatus()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/minha-conta")
    public ResponseEntity<MinhaContaResponse> consultarMinhaConta(Principal principal) {
        MinhaContaResponse response = minhaContaService.consultar(obterUidDoUsuarioAutenticado(principal));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/minha-conta")
    public ResponseEntity<MinhaContaResponse> atualizarMinhaConta(
            Principal principal,
            @RequestBody MinhaContaUpdateRequest request
    ) {
        MinhaContaResponse response = minhaContaService.atualizar(obterUidDoUsuarioAutenticado(principal), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{usuarioId}/anonimizar")
    public ResponseEntity<AnonimizarUsuarioResponse> anonimizarUsuario(
            Principal principal,
            @PathVariable UUID usuarioId
    ) {
        UUID actorId = obterUidDoUsuarioAutenticado(principal);
        AnonimizarUsuarioResponse response = anonimizacaoService.anonimizar(
                actorId,
                new AnonimizarUsuarioRequest(usuarioId)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> excluirUsuario(
            Principal principal,
            @PathVariable UUID usuarioId
    ) {
        UUID actorId = obterUidDoUsuarioAutenticado(principal);
        anonimizacaoService.anonimizar(actorId, new AnonimizarUsuarioRequest(usuarioId));
        return ResponseEntity.noContent().build();
    }

    private UUID obterUidDoUsuarioAutenticado(Principal principal) {
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
