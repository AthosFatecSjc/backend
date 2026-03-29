package com.energia.backend.controller;

import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.Usuario;
import com.energia.backend.service.MinhaContaService;
import com.energia.backend.service.UsuarioCadastroService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioControllerTest {

    @Test
    void deveRetornarCreatedComMensagemDeSucesso() {
        UsuarioCadastroService service = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        UsuarioController controller = new UsuarioController(service, minhaContaService);

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

    @Test
    void deveConsultarMinhaContaDoUsuarioAutenticado() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        UsuarioController controller = new UsuarioController(cadastroService, minhaContaService);
        UUID userId = UUID.randomUUID();

        LocalDateTime dataCadastro = LocalDateTime.now().minusDays(2);
        MinhaContaResponse conta = new MinhaContaResponse(
                "Maria Silva",
                "maria@teste.com",
                "11999998888",
            StatusUsuario.PENDENTE,
                dataCadastro
        );

        when(minhaContaService.consultar(userId)).thenReturn(conta);

        Principal principal = () -> userId.toString();
        ResponseEntity<MinhaContaResponse> response = controller.consultarMinhaConta(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("maria@teste.com", response.getBody().getEmail());
        assertEquals(StatusUsuario.PENDENTE, response.getBody().getStatus());
        assertEquals(dataCadastro, response.getBody().getDataCadastro());
    }

    @Test
    void deveAtualizarSomenteCamposPermitidosDaMinhaConta() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        UsuarioController controller = new UsuarioController(cadastroService, minhaContaService);
        UUID userId = UUID.randomUUID();

        LocalDateTime dataCadastroOriginal = LocalDateTime.now().minusDays(10);
        MinhaContaResponse contaAtualizada = new MinhaContaResponse(
                "Maria Atualizada",
                "maria@teste.com",
                "11911112222",
                StatusUsuario.PENDENTE,
                dataCadastroOriginal
        );

        when(minhaContaService.atualizar(any(UUID.class), any(MinhaContaUpdateRequest.class)))
                .thenReturn(contaAtualizada);

        MinhaContaUpdateRequest request = new MinhaContaUpdateRequest();
        request.setNomeCompleto("Maria Atualizada");
        request.setTelefone("11911112222");

        Principal principal = () -> userId.toString();
        ResponseEntity<MinhaContaResponse> response = controller.atualizarMinhaConta(principal, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Maria Atualizada", response.getBody().getNomeCompleto());
        assertEquals("11911112222", response.getBody().getTelefone());
        assertEquals("maria@teste.com", response.getBody().getEmail());
        assertEquals(StatusUsuario.PENDENTE, response.getBody().getStatus());
        assertEquals(dataCadastroOriginal, response.getBody().getDataCadastro());
    }

    @Test
    void deveRetornarUnauthorizedQuandoPrincipalNaoExisteNaMinhaConta() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        UsuarioController controller = new UsuarioController(cadastroService, minhaContaService);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.consultarMinhaConta(null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void deveRetornarUnauthorizedQuandoPrincipalNaoPossuiUuidValido() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        UsuarioController controller = new UsuarioController(cadastroService, minhaContaService);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.consultarMinhaConta(() -> "maria@teste.com")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Identificador do usuario autenticado invalido.", exception.getReason());
    }
}
