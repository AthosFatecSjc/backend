package com.energia.backend.service;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.dto.Usuario;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioCadastroServiceTest {

    @Test
    void deveCadastrarUsuarioComStatusPendenteESenhaHasheada() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TermsService termsService = mock(TermsService.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);

        UsuarioCadastroService service =
                new UsuarioCadastroService(usuarioRepository, termsService, statusRepository, userStatusRepository);

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);

        // simula save retornando entidade com ID
        when(usuarioRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> {
            AppUserEntity user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Maria Silva");
        request.setEmail("  MARIA@TESTE.COM ");
        request.setSenha("SenhaFuerte123");
        request.setTelefone(" 11999998888 ");
        request.setTermsIds(List.of(UUID.randomUUID()));

        Usuario usuario = service.cadastrar(request);

        assertEquals(StatusUsuario.PENDENTE, usuario.getStatus());
        assertEquals("maria@teste.com", usuario.getEmail());
        assertEquals("11999998888", usuario.getTelefone());
        assertNotNull(usuario.getDataCadastro());
        assertNotNull(usuario.getSenhaHash());
        assertNotEquals("SenhaFuerte123", usuario.getSenhaHash());
        assertTrue(usuario.getSenhaHash().startsWith("PBKDF2$"));
        assertEquals(4, usuario.getSenhaHash().split("\\$").length);

        verify(usuarioRepository).save(any(AppUserEntity.class));
    }

    @Test
    void deveRejeitarEmailDuplicado() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TermsService termsService = mock(TermsService.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);

        UsuarioCadastroService service =
                new UsuarioCadastroService(usuarioRepository, termsService, statusRepository, userStatusRepository);

        when(usuarioRepository.existsByEmail("duplicado@teste.com"))
                .thenReturn(true);

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Joao");
        request.setEmail("DUPLICADO@TESTE.COM");
        request.setSenha("SenhaFuerte123");
        request.setTermsIds(List.of(UUID.randomUUID()));

        EmailJaCadastradoException exception = assertThrows(
                EmailJaCadastradoException.class,
                () -> service.cadastrar(request)
        );

        assertEquals("E-mail ja cadastrado.", exception.getMessage());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRejeitarCadastroSemAceiteDosTermosObrigatorios() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TermsService termsService = mock(TermsService.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);

        UsuarioCadastroService service =
                new UsuarioCadastroService(usuarioRepository, termsService, statusRepository, userStatusRepository);

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Joao");
        request.setEmail("joao@teste.com");
        request.setSenha("SenhaFuerte123");

        TermoNaoEncontradoException exception = assertThrows(
                TermoNaoEncontradoException.class,
                () -> service.cadastrar(request)
        );

        assertEquals("Aceite dos termos obrigatorios e obrigatorio.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(termsService, never()).registrarTermosAceitos(any(), any());
    }
}
