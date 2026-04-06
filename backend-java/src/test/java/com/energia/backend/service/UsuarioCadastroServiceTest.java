package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.energia.backend.dto.Usuario;
import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;
import com.energia.backend.repository.UsuarioRepository;

class UsuarioCadastroServiceTest {

    @Test
    void deveCadastrarUsuarioComStatusPendenteESenhaHasheada() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TermsService termsService = mock(TermsService.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("encoded_SenhaFuerte123");

        UsuarioCadastroService service =
                new UsuarioCadastroService(usuarioRepository, termsService, statusRepository, userStatusRepository, passwordEncoder);

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
        // Verifica que a senha foi encriptada (não é igual à original)
        assertNotEquals("SenhaFuerte123", usuario.getSenhaHash());
        // Verifica que o PasswordEncoder foi usado (mock adiciona prefixo "encoded_")
        assertEquals("encoded_SenhaFuerte123", usuario.getSenhaHash());
    }

    @Test
    void deveRejeitarEmailDuplicado() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        TermsService termsService = mock(TermsService.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);

        PasswordEncoder passwordEncoder = new MockPasswordEncoder();
        UsuarioCadastroService service =
                new UsuarioCadastroService(usuarioRepository, termsService, statusRepository, userStatusRepository, passwordEncoder);

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
    }

    /**
     * Mock PasswordEncoder que prefixia a senha com "encoded_" para fins de teste.
     * Em produção, seria BCryptPasswordEncoder.
     */
    private static class MockPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "encoded_" + rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }

    private static class InMemoryCadastroRepository implements UsuarioCadastroRepository {
        private final Set<String> existingEmails = new HashSet<>();

        @Override
        public boolean existsByEmail(String email) {
            return existingEmails.contains(email);
        }

        @Override
        public Usuario save(Usuario usuario) {
            existingEmails.add(usuario.getEmail());
            return usuario;
        }

        void markAsExisting(String email) {
            existingEmails.add(email);
        }
    }
}
