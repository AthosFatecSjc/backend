package com.energia.backend.controller;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.dto.Usuario;
import com.energia.backend.service.TermsService;
import com.energia.backend.service.UsuarioCadastroService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {

    @Test
    void deveRetornarCreatedComMensagemDeSucesso() {

        UsuarioCadastroService service = mock(UsuarioCadastroService.class);
        TermsService termsService = mock(TermsService.class);

        UsuarioController controller = new UsuarioController(service);

        Usuario usuario = new Usuario();
        usuario.setEmail("novo@teste.com");
        usuario.setStatus(StatusUsuario.PENDENTE);

        when(service.cadastrar(any(UsuarioCadastroRequest.class))).thenReturn(usuario);

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Novo Usuario");
        request.setEmail("novo@teste.com");
        request.setSenha("SenhaFuerte123");

        request.setTermsIds(List.of(UUID.randomUUID()));

        ResponseEntity<UsuarioCadastroResponse> response = controller.cadastrar(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Cadastro realizado com sucesso. Aguardando aprovacao do administrador.", response.getBody().getMensagem());
        assertEquals("novo@teste.com", response.getBody().getEmail());
        assertEquals(StatusUsuario.PENDENTE, response.getBody().getStatus());

        verify(termsService, times(1))
                .registrarTermosAceitos(any(), any());
    }
}