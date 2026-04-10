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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.AnonimizarUsuarioRequest;
import com.energia.backend.dto.AnonimizarUsuarioResponse;
import com.energia.backend.dto.AprovacaoRejeicaoUsuarioRequest;
import com.energia.backend.dto.AtualizarEmailRequest;
import com.energia.backend.dto.HistoricoTermoResponse;
import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.dto.RegistrarTermosRequest;
import com.energia.backend.dto.TermosPendentesResponse;
import com.energia.backend.dto.Usuario;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.service.AnonimizacaoService;
import com.energia.backend.service.MinhaContaService;
import com.energia.backend.service.TermsService;
import com.energia.backend.service.UsuarioCadastroService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioCadastroService cadastroService;
    private final MinhaContaService minhaContaService;
    private final AnonimizacaoService anonimizacaoService;
    private final TermsService termsService;
    private final AppUserJpaRepository appUserRepository;

    public UsuarioController(
            UsuarioCadastroService cadastroService,
            MinhaContaService minhaContaService,
            AnonimizacaoService anonimizacaoService,
            TermsService termsService,
            AppUserJpaRepository appUserRepository
    ) {
        this.cadastroService = cadastroService;
        this.minhaContaService = minhaContaService;
        this.anonimizacaoService = anonimizacaoService;
        this.termsService = termsService;
        this.appUserRepository = appUserRepository;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
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
        MinhaContaResponse response = minhaContaService.consultar(obterUid(principal));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/minha-conta")
    public ResponseEntity<MinhaContaResponse> atualizarMinhaConta(
            Principal principal,
            @RequestBody MinhaContaUpdateRequest request
    ) {
        MinhaContaResponse response = minhaContaService.atualizar(obterUid(principal), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MinhaContaResponse> atualizarEmail(
            Principal principal,
            @PathVariable("id") UUID usuarioId,
            @RequestBody AtualizarEmailRequest request
    ) {
        MinhaContaResponse response = minhaContaService.atualizarEmail(
                obterUid(principal),
                usuarioId,
                request
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meus-termos/historico")
    public ResponseEntity<List<HistoricoTermoResponse>> listarHistoricoTermos(Principal principal) {
        return ResponseEntity.ok(termsService.listarHistorico(obterUid(principal)));
    }

    @GetMapping("/meus-termos/pendentes")
    public ResponseEntity<List<TermosPendentesResponse>> listarTermosPendentes(Principal principal) {
        return ResponseEntity.ok(termsService.listarPendenciasDeAcesso(obterUid(principal)));
    }

    @PostMapping("/meus-termos/aceites")
    public ResponseEntity<Void> aceitarTermosPendentes(
            Principal principal,
            @RequestBody RegistrarTermosRequest request,
            HttpServletRequest httpRequest
    ) {
        termsService.registrarTermosAceitos(
                request.getTermsIds(),
                obterUsuario(principal),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meus-termos/{termId}/revogacao")
    public ResponseEntity<Void> revogarTermoOpcional(
            Principal principal,
            @PathVariable UUID termId,
            HttpServletRequest httpRequest
    ) {
        termsService.revogarConsentimentoOpcional(
                obterUsuario(principal),
                termId,
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{usuarioId}/anonimizar")
    public ResponseEntity<AnonimizarUsuarioResponse> anonimizarUsuario(
            Principal principal,
            @PathVariable UUID usuarioId
    ) {
        UUID actorId = obterUid(principal);
        AnonimizarUsuarioResponse response = anonimizacaoService.anonimizar(
                actorId,
                new AnonimizarUsuarioRequest(usuarioId)
        );
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

    private AppUserEntity obterUsuario(Principal principal) {
        UUID userId = obterUid(principal);
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario autenticado nao encontrado."));
    }
}
