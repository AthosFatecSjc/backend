package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.energia.backend.dto.Usuario;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

class UsuarioCadastroServiceTest {

    @Test
    void deveCadastrarUsuarioComStatusPendenteESenhaHasheada() {
        UsuarioCadastroRepository usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        TermsUserService termsUserService = mock(TermsUserService.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        TermsRepository termsRepository = mock(TermsRepository.class);

        when(passwordEncoder.encode(any())).thenReturn("encoded_SenhaFuerte123");
        when(usuarioCadastroRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioCadastroRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(termsRepository.findActiveRequiredByReferenceTime(any())).thenReturn(List.of());
        when(termsUserService.checkRequiredTerms(any(), any(), any())).thenReturn(true);

        UsuarioCadastroService service = new UsuarioCadastroService(
                usuarioCadastroRepository,
                appUserRepository,
                termsUserService,
                statusRepository,
                userStatusRepository,
                passwordEncoder
        );

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Maria Silva");
        request.setEmail("  MARIA@TESTE.COM ");
        request.setSenha("SenhaFuerte123");
        request.setTelefone(" 11999998888 ");
        request.setTermsNames(List.of("TERMS_OF_USE", "PRIVACY_POLICY"));

        Usuario usuario = service.cadastrar(request);

        assertEquals(StatusUsuario.PENDENTE, usuario.getStatus());
        assertEquals("maria@teste.com", usuario.getEmail());
        assertEquals("11999998888", usuario.getTelefone());
        assertNotNull(usuario.getDataCadastro());
        assertNotNull(usuario.getSenhaHash());
        assertNotEquals("SenhaFuerte123", usuario.getSenhaHash());
        assertEquals("encoded_SenhaFuerte123", usuario.getSenhaHash());
        verifyNoInteractions(appUserRepository, termsUserService);
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        UsuarioCadastroRepository usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        TermsUserService termsUserService = mock(TermsUserService.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        PasswordEncoder passwordEncoder = new MockPasswordEncoder();

        when(usuarioCadastroRepository.existsByEmail("duplicado@teste.com")).thenReturn(true);
        when(termsUserService.checkRequiredTerms(any(), any(), any())).thenReturn(true);

        UsuarioCadastroService service = new UsuarioCadastroService(
                usuarioCadastroRepository,
                appUserRepository,
                termsUserService,
                statusRepository,
                userStatusRepository,
                passwordEncoder
        );

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Joao");
        request.setEmail("DUPLICADO@TESTE.COM");
        request.setSenha("SenhaFuerte123");
        request.setTermsNames(List.of("TERMS_OF_USE", "PRIVACY_POLICY"));

        EmailJaCadastradoException exception = assertThrows(
                EmailJaCadastradoException.class,
                () -> service.cadastrar(request)
        );

        assertEquals("E-mail ja cadastrado.", exception.getMessage());
    }

    private static class MockPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "encoded_" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }
}
