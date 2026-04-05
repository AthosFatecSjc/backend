package com.energia.backend.service;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.Usuario;
import com.energia.backend.repository.UsuarioCadastroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioCadastroServiceTest {

    @Test
    void deveCadastrarUsuarioComStatusPendenteESenhaHasheada() {
        InMemoryCadastroRepository repository = new InMemoryCadastroRepository();
        PasswordEncoder passwordEncoder = new MockPasswordEncoder(); // Mock que transforma: "password" -> "encoded_password"
        UsuarioCadastroService service = new UsuarioCadastroService(repository, passwordEncoder);

        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Maria Silva");
        request.setEmail("  MARIA@TESTE.COM ");
        request.setSenha("SenhaFuerte123");
        request.setTelefone(" 11999998888 ");

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
        InMemoryCadastroRepository repository = new InMemoryCadastroRepository();
        repository.markAsExisting("duplicado@teste.com");

        PasswordEncoder passwordEncoder = new MockPasswordEncoder();
        UsuarioCadastroService service = new UsuarioCadastroService(repository, passwordEncoder);
        UsuarioCadastroRequest request = new UsuarioCadastroRequest();
        request.setNomeCompleto("Joao");
        request.setEmail("DUPLICADO@TESTE.COM");
        request.setSenha("SenhaFuerte123");

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
