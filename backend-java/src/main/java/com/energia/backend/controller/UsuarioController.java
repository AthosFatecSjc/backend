package com.energia.backend.controller;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.model.Usuario;
import com.energia.backend.service.UsuarioCadastroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioCadastroService cadastroService;

    public UsuarioController(UsuarioCadastroService cadastroService) {
        this.cadastroService = cadastroService;
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
}
