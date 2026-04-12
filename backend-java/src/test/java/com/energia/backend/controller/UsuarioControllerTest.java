package com.energia.backend.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.AprovacaoRejeicaoUsuarioRequest;
import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.dto.Usuario;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.dto.term.AcceptedTermRequestDto;
import com.energia.backend.mapper.user.AppUserMapper;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.user.AppUserModel;
import com.energia.backend.model.user.UserRegistrationModel;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.service.AnonimizacaoService;
import com.energia.backend.service.MinhaContaService;
import com.energia.backend.service.TermsService;
import com.energia.backend.service.UsuarioCadastroService;

import jakarta.servlet.http.HttpServletRequest;

class UsuarioControllerTest {

    private UsuarioController criarController(
            UsuarioCadastroService cadastroService,
            MinhaContaService minhaContaService,
            AnonimizacaoService anonimizacaoService
    ) {
        TermsService termsService = mock(TermsService.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        AppUserMapper appUserMapper = mock(AppUserMapper.class);
        
        return new UsuarioController(
                cadastroService,
                minhaContaService,
                anonimizacaoService,
                termsService,
                appUserRepository,
                appUserMapper
        );
    }

    @Test
    void deveAlterarStatusPorPatchUnico() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);

        UUID usuarioId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AprovacaoRejeicaoUsuarioRequest request = new AprovacaoRejeicaoUsuarioRequest();
        request.setStatus(StatusUsuario.ATIVO);

        ResponseEntity<String> response = controller.alterarStatusUsuario(usuarioId, request, () -> adminId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Status do usuario atualizado com sucesso.", response.getBody());
        verify(cadastroService).alterarStatusUsuario(eq(usuarioId), eq(adminId), eq(StatusUsuario.ATIVO), eq(null));
    }

    @Test
    void devePropagarErroDoServiceQuandoMotivoForInvalidoNoPatchDeStatus() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);

        AprovacaoRejeicaoUsuarioRequest request = new AprovacaoRejeicaoUsuarioRequest();
        request.setStatus(StatusUsuario.REJEITADO);
        request.setMotivo("   ");

        doThrow(new IllegalArgumentException("Motivo da rejeicao e obrigatorio."))
                .when(cadastroService)
                .alterarStatusUsuario(any(UUID.class), any(UUID.class), eq(StatusUsuario.REJEITADO), eq("   "));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.alterarStatusUsuario(UUID.randomUUID(), request, () -> UUID.randomUUID().toString())
        );

        assertEquals("Motivo da rejeicao e obrigatorio.", exception.getMessage());
    }

    @Test
    void deveRetornarUnauthorizedQuandoPrincipalForInvalidoNoPatchDeStatus() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);

        AprovacaoRejeicaoUsuarioRequest request = new AprovacaoRejeicaoUsuarioRequest();
        request.setStatus(StatusUsuario.ATIVO);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.alterarStatusUsuario(UUID.randomUUID(), request, () -> "admin@email.com")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Identificador do usuario autenticado invalido.", exception.getReason());
    }

    @Test
    void deveRetornarCreatedComMensagemDeSucesso() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);

        AppUserModel user = AppUserModel.builder()
                .email("novo@teste.com")
                .status(StatusUsuario.PENDENTE)
                .build();

        when(cadastroService.cadastrar(any(UserRegistrationModel.class))).thenReturn(user);

        AcceptedTermRequestDto term1 = AcceptedTermRequestDto.builder()
            .id(UUID.randomUUID())
            .version(1)
            .build();

        AcceptedTermRequestDto term2 = AcceptedTermRequestDto.builder()
            .id(UUID.randomUUID())
            .version(1)
            .build();

        UsuarioCadastroRequest request = UsuarioCadastroRequest.builder()
                .nomeCompleto("Novo Usuario")
                .email("novo@teste.com")
                .senha("SenhaFuerte123")
                .terms(List.of(term1, term2))
                .build();
        
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        ResponseEntity<UsuarioCadastroResponse> response = controller.cadastrar(request, httpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Cadastro realizado com sucesso. Aguardando aprovacao do administrador.", response.getBody().getMensagem());
        assertEquals("novo@teste.com", response.getBody().getEmail());
        assertEquals(StatusUsuario.PENDENTE, response.getBody().getStatus());
    }

    @Test
    void deveConsultarMinhaContaDoUsuarioAutenticado() {
        UsuarioCadastroService cadastroService = mock(UsuarioCadastroService.class);
        MinhaContaService minhaContaService = mock(MinhaContaService.class);
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);
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
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);
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
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);

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
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        UsuarioController controller = criarController(cadastroService, minhaContaService, anonimizacaoService);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.consultarMinhaConta(() -> "maria@teste.com")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Identificador do usuario autenticado invalido.", exception.getReason());
    }
}
