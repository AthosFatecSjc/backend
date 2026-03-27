package com.energia.backend.controller;

import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.model.Usuario;
import com.energia.backend.service.MinhaContaService;
import com.energia.backend.service.UsuarioCadastroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioCadastroService cadastroService;
    private final MinhaContaService minhaContaService;

    public UsuarioController(UsuarioCadastroService cadastroService, MinhaContaService minhaContaService) {
        this.cadastroService = cadastroService;
        this.minhaContaService = minhaContaService;
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
        MinhaContaResponse response = minhaContaService.consultar(obterEmailDoUsuarioAutenticado(principal));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/minha-conta")
    public ResponseEntity<MinhaContaResponse> atualizarMinhaConta(
            Principal principal,
            @RequestBody MinhaContaUpdateRequest request
    ) {
        MinhaContaResponse response = minhaContaService.atualizar(obterEmailDoUsuarioAutenticado(principal), request);
        return ResponseEntity.ok(response);
    }

    private String obterEmailDoUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }
        return principal.getName();
    }
}
