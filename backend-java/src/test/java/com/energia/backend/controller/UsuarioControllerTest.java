package com.energia.backend.controller;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.Usuario;
import com.energia.backend.service.UsuarioCadastroService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioControllerTest {

    @Test
    void deveRetornarCreatedComMensagemDeSucesso() {
        UsuarioCadastroService service = mock(UsuarioCadastroService.class);
        UsuarioController controller = new UsuarioController(service);

        Usuario usuario = new Usuario();
        usuario.setEmail("novo@teste.com");
        usuario.setStatus(StatusUsuario.PENDENTE);

        when(service.cadastrar(any(UsuarioCadastroRequest.class))).thenReturn(usuario);

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Novo Usuario");
        request.setEmail("novo@teste.com");
        request.setSenha("SenhaFuerte123");

        ResponseEntity<UsuarioCadastroResponse> response = controller.cadastrar(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Cadastro realizado com sucesso. Aguardando aprovacao do administrador.", response.getBody().getMensagem());
        assertEquals("novo@teste.com", response.getBody().getEmail());
        assertEquals(StatusUsuario.PENDENTE, response.getBody().getStatus());
    }
}
