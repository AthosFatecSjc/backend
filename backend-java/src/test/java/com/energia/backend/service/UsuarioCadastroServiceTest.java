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
import com.energia.backend.model.user.AppUserModel;
import com.energia.backend.model.user.UserRegistrationModel;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;

class UsuarioCadastroServiceTest {

    @Test
    void deveCadastrarUsuarioComStatusPendenteESenhaHasheada() {
        UsuarioCadastroRepository usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        TermsService termsService = mock(TermsService.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(passwordEncoder.encode(any())).thenReturn("encoded_SenhaFuerte123");
        when(usuarioCadastroRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioCadastroRepository.save(any(AppUserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioCadastroService service = new UsuarioCadastroService(
                usuarioCadastroRepository,
                appUserRepository,
                termsService,
                userStatusService,
                passwordEncoder
        );

        UserRegistrationModel model = UserRegistrationModel.builder()
            .user(AppUserModel.builder()
                .fullName("Maria Silva")
                .email("  MARIA@TESTE.COM ")
                .password("SenhaFuerte123")
                .phone(" 11999998888 ")
                .build())
            .acceptedTerms(List.of())
            .build();

        AppUserModel registeredUser = service.cadastrar(model);

        assertEquals(StatusUsuario.PENDENTE, registeredUser.getStatus());
        assertEquals("maria@teste.com", registeredUser.getEmail());
        assertEquals("11999998888", registeredUser.getPhone());
        assertNotNull(registeredUser.getCreatedAt());
        assertNotNull(registeredUser.getPassword());
        assertNotEquals("SenhaFuerte123", registeredUser.getPassword());
        assertEquals("encoded_SenhaFuerte123", registeredUser.getPassword());
        verifyNoInteractions(appUserRepository, termsService);
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        UsuarioCadastroRepository usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        TermsService termsService = mock(TermsService.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        PasswordEncoder passwordEncoder = new MockPasswordEncoder();

        when(usuarioCadastroRepository.existsByEmail("duplicado@teste.com")).thenReturn(true);

        UsuarioCadastroService service = new UsuarioCadastroService(
                usuarioCadastroRepository,
                appUserRepository,
                termsService,
                userStatusService,
                passwordEncoder
        );

        UserRegistrationModel model = UserRegistrationModel.builder()
            .user(AppUserModel.builder()
                .fullName("Joao")
                .email("DUPLICADO@TESTE.COM")
                .password("SenhaFuerte123")
                .build())
            .acceptedTerms(List.of())
            .build();

        AppUserModel registeredUser = service.cadastrar(model);

        EmailJaCadastradoException exception = assertThrows(
                EmailJaCadastradoException.class,
                () -> service.cadastrar(model)
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
