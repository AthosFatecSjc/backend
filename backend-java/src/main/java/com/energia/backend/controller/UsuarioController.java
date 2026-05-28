package com.energia.backend.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.AlterarRoleUsuarioRequest;
import com.energia.backend.dto.DeletarUsuarioRequest;
import com.energia.backend.dto.DeletarUsuarioResponse;
import com.energia.backend.dto.AprovacaoRejeicaoUsuarioRequest;
import com.energia.backend.dto.AtualizarEmailRequest;
import com.energia.backend.dto.UserTermResponse;
import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.dto.RegistrarTermosRequest;
import com.energia.backend.dto.TermosResponse;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.service.DelecaoService;
import com.energia.backend.service.MinhaContaService;
import com.energia.backend.service.TermsUserService;
import com.energia.backend.service.UsuarioCadastroService;

import com.energia.backend.service.UserRoleService;
import com.energia.backend.service.LoginSharingService;
import com.energia.backend.dto.LoginSharingRequestDto;
import com.energia.backend.dto.LoginSharingPublicStatusDto;
import com.energia.backend.dto.LoginSharingResponseDto;
import com.energia.backend.dto.LoginSharingConsentRequest;
import com.energia.backend.dto.LoginSharingUserDataDto;


import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "${CORS_ORIGINS:http://localhost:3000}")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioCadastroService cadastroService;
    private final MinhaContaService minhaContaService;
    private final DelecaoService delecaoService;
    private final AppUserJpaRepository appUserRepository;
    private final TermsUserService termsUserService;
    private final UserRoleService userRoleService;
    private final LoginSharingService loginSharingService;

    @Autowired
    public UsuarioController(
            UsuarioCadastroService cadastroService,
            MinhaContaService minhaContaService,
            DelecaoService delecaoService,
            AppUserJpaRepository appUserRepository,
            TermsUserService termsUserService,
            UserRoleService userRoleService,
            LoginSharingService loginSharingService
    ) {
        this.cadastroService = cadastroService;
        this.minhaContaService = minhaContaService;
        this.delecaoService = delecaoService;
        this.appUserRepository = appUserRepository;
        this.termsUserService = termsUserService;
        this.userRoleService = userRoleService;
        this.loginSharingService = loginSharingService;
    }

    // Backwards-compatible constructor used by existing tests that don't provide LoginSharingService
    public UsuarioController(
            UsuarioCadastroService cadastroService,
            MinhaContaService minhaContaService,
            DelecaoService anonimizacaoService,
            AppUserJpaRepository appUserRepository,
            TermsUserService termsUserService,
            UserRoleService userRoleService
    ) {
        this(cadastroService, minhaContaService, anonimizacaoService, appUserRepository, termsUserService, userRoleService, null);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> alterarStatusUsuario(
            @PathVariable("id") UUID usuarioId,
            @RequestBody AprovacaoRejeicaoUsuarioRequest request,
            Principal principal) {
        UUID adminId = obterUid(principal);
        cadastroService.alterarStatusUsuario(usuarioId, adminId, request.getStatus(), request.getMotivo());
        return ResponseEntity.ok("Status do usuario atualizado com sucesso.");
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> alterarRoleUsuario(
            @PathVariable("id") UUID usuarioId,
            @RequestBody AlterarRoleUsuarioRequest request,
            Principal principal
    ) {
        UUID adminId = obterUid(principal);
        userRoleService.alterarRoleUsuario(usuarioId, adminId, request.getRoleName());
        return ResponseEntity.ok("Role do usuario atualizado com sucesso.");
    }

    @PostMapping(
        value = "/cadastro",
        consumes = "application/json",
        produces = "application/json"
    )
    public ResponseEntity<UsuarioCadastroResponse> cadastrar(
            @Valid @RequestBody UsuarioCadastroRequest request,
            HttpServletRequest httpRequest) {
        log.info("User registration requested for email={} from IP={}",
                request.getEmail(),
                httpRequest.getRemoteAddr());

        AppUserEntity registeredUser = cadastroService.cadastrar(request);

        log.info("User registration successful for email={} with id={}",
                registeredUser.getEmail(),
                registeredUser.getId());

        UsuarioCadastroResponse response = UsuarioCadastroResponse.builder()
                .mensagem("Registration completed successfully. Awaiting administrator approval")
                .email(registeredUser.getEmail())
                .status(new ArrayList<StatusUsuario>(List.of(StatusUsuario.PENDENTE)))
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/minha-conta")
    public ResponseEntity<MinhaContaResponse> consultarMinhaConta(Principal principal) {
        MinhaContaResponse response = minhaContaService.consultar(obterUid(principal));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/minha-conta")
    public ResponseEntity<MinhaContaResponse> atualizarMinhaConta(
            Principal principal,
            @RequestBody MinhaContaUpdateRequest request) {
        MinhaContaResponse response = minhaContaService.atualizar(obterUid(principal), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MinhaContaResponse> atualizarEmail(
            Principal principal,
            @PathVariable("id") UUID usuarioId,
            @RequestBody AtualizarEmailRequest request) {
        MinhaContaResponse response = minhaContaService.atualizarEmail(
                obterUid(principal),
                usuarioId,
                request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meus-termos/historico")
    public ResponseEntity<List<UserTermResponse>> listarHistoricoTermos(Principal principal) {
        return ResponseEntity.ok(termsUserService.listarHistorico(obterUid(principal)));
    }

    @GetMapping("/meus-termos/pendentes")
    public ResponseEntity<List<TermosResponse>> listarTermosPendentes(Principal principal) {
        List<TermosResponse> termosResponses = termsUserService
            .listarTermosPendentes(obterUid(principal), false)
            .stream()
            .map(TermosResponse::fromEntity)
            .toList();

    return ResponseEntity.ok(termosResponses);
    }

    @PostMapping("/meus-termos/aceites")
    public ResponseEntity<Void> aceitarTermosPendentes(
            Principal principal,
            @RequestBody RegistrarTermosRequest request,
            HttpServletRequest httpRequest) {
        termsUserService.aprovarTermos(
                request.getTermsIds(),
                obterUsuario(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meus-termos/revogacao")
    public ResponseEntity<Void> revogarTermoOpcional(
            Principal principal,
            @RequestBody RegistrarTermosRequest request,
            HttpServletRequest httpRequest) {
        termsUserService.revogarTermos(
            request.getTermsIds(),
            obterUsuario(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{usuarioId}/deletar")
    public ResponseEntity<DeletarUsuarioResponse> deletarUsuario(
            Principal principal,
            @PathVariable UUID usuarioId) {
        UUID actorId = obterUid(principal);
        DeletarUsuarioResponse response = delecaoService.deletar(
                actorId,
                new DeletarUsuarioRequest(usuarioId));
        return ResponseEntity.ok(response);
    }

    // ============ LOGIN SHARING ENDPOINTS (FOR TESTING - OAuth-like flow) ============

    @PostMapping("/login-sharing/request")
    public ResponseEntity<LoginSharingResponseDto> requestLoginData(
            @Valid @RequestBody LoginSharingRequestDto request) {
        log.info("External agent {} requesting login data for user {}",
                request.getExternalAgentName(), request.getUserEmail());
        LoginSharingResponseDto response = loginSharingService.requestLoginData(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/login-sharing/pending")
    public ResponseEntity<java.util.List<LoginSharingResponseDto>> getPendingRequests(
            Principal principal,
            @RequestParam(value = "userId", required = false) UUID userId) {
        UUID resolvedUserId = resolverUsuarioId(principal, userId);
        log.info("User {} fetching pending login sharing requests", resolvedUserId);
        java.util.List<LoginSharingResponseDto> requests = loginSharingService.getUserPendingRequests(resolvedUserId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/login-sharing/{requestId}/consent")
    public ResponseEntity<LoginSharingResponseDto> getRequestForConsent(
            @PathVariable UUID requestId,
            Principal principal,
            @RequestParam(value = "userId", required = false) UUID userId) {
        UUID resolvedUserId = resolverUsuarioId(principal, userId);
        log.info("User {} fetching consent for request {}", resolvedUserId, requestId);
        LoginSharingResponseDto response = loginSharingService.getRequestForConsent(requestId, resolvedUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login-sharing/{requestId}")
    public ResponseEntity<LoginSharingResponseDto> getRequestById(
            @PathVariable UUID requestId,
            Principal principal,
            @RequestParam(value = "userId", required = false) UUID userId) {
        UUID resolvedUserId = resolverUsuarioId(principal, userId);
        log.info("User {} fetching login sharing request {}", resolvedUserId, requestId);
        LoginSharingResponseDto response = loginSharingService.getRequestForConsent(requestId, resolvedUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/login-sharing/public/{requestId}", "/login-sharing/request/{requestId}"})
    public ResponseEntity<LoginSharingPublicStatusDto> getRequestByIdDirect(
            @PathVariable UUID requestId,
            @RequestParam(value = "token", required = false) String token) {
        log.info("Public status requested for login sharing request {}", requestId);
        LoginSharingPublicStatusDto response = loginSharingService.getPublicRequestStatus(requestId, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-sharing/{requestId}/respond")
    public ResponseEntity<LoginSharingResponseDto> respondToRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody LoginSharingConsentRequest consent,
            Principal principal,
            @RequestParam(value = "userId", required = false) UUID userId) {
        UUID resolvedUserId = resolverUsuarioId(principal, userId);
        log.info("User {} responding to login sharing request {}", resolvedUserId, requestId);
        LoginSharingResponseDto response = loginSharingService.respondToRequest(requestId, resolvedUserId, consent);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/login-sharing/{requestId}/revoke", "/login-sharing/{requestId}/revogar"})
    public ResponseEntity<LoginSharingResponseDto> revokeRequest(
            @PathVariable UUID requestId,
            Principal principal,
            @RequestParam(value = "userId", required = false) UUID userId) {
        UUID resolvedUserId = resolverUsuarioId(principal, userId);
        log.info("User {} revoking login sharing request {}", resolvedUserId, requestId);
        LoginSharingResponseDto response = loginSharingService.revokeRequest(requestId, resolvedUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/login-sharing/public/{requestId}/user-data", "/login-sharing/{requestId}/user-data"})
    public ResponseEntity<LoginSharingUserDataDto> getUserDataByRequest(
            @PathVariable UUID requestId,
            @RequestParam(value = "token", required = false) String token) {
        log.info("Public user data requested for login sharing request {}", requestId);
        LoginSharingUserDataDto userData = loginSharingService.getPublicUserDataByRequest(requestId, token);
        return ResponseEntity.ok(userData);
    }

    @GetMapping({"/login-sharing/history", "/login-sharing/historico"})
    public ResponseEntity<java.util.List<LoginSharingResponseDto>> getUserSharingHistory(
            Principal principal,
            @RequestParam(value = "userId", required = false) UUID userId) {
        UUID resolvedUserId = resolverUsuarioId(principal, userId);
        log.info("User {} fetching login sharing history", resolvedUserId);
        java.util.List<LoginSharingResponseDto> requests = loginSharingService.getUserAllRequests(resolvedUserId);
        return ResponseEntity.ok(requests);
    }

    private UUID resolverUsuarioId(Principal principal, UUID userId) {
        if (userId != null) {
            return userId;
        }

        return obterUid(principal);
    }

    private UUID obterUid(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }

        try {
            return UUID.fromString(principal.getName().trim());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Identificador do usuario autenticado invalido.");
        }
    }

    private AppUserEntity obterUsuario(Principal principal) {
        UUID userId = obterUid(principal);
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario autenticado nao encontrado."));
    }
}
