package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InOrder;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.model.user.AppUserModel;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.JpaUsuarioCadastroRepository;
import com.energia.backend.repository.RoleJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import com.energia.backend.repository.UsuarioCadastroRepository;
import com.energia.backend.repository.UsuarioRepository;

class UsuarioCadastroServiceTest {

        @Test
        void deveCadastrarUsuarioComStatusPendenteESenhaHasheada() {
                UsuarioCadastroRepository usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
                AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
                TermsUserService termsUserService = mock(TermsUserService.class);
                StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
                UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
                TermsService termsService = mock(TermsService.class);
                UserStatusService userStatusService = mock(UserStatusService.class);
                PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
                TermsRepository termsRepository = mock(TermsRepository.class);
                JpaUsuarioCadastroRepository jpaUsuarioCadastroRepository = mock(JpaUsuarioCadastroRepository.class);
                RoleJpaRepository roleJpaRepository = mock(RoleJpaRepository.class);
                UsuarioRepository userRepository = mock(UsuarioRepository.class);
        ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService =
                mock(ExternalUserPrivacyRegistryService.class);

                when(passwordEncoder.encode(any())).thenReturn("encoded_SenhaFuerte123");
                when(usuarioCadastroRepository.existsByEmail(anyString())).thenReturn(false);
                when(usuarioCadastroRepository.save(any(AppUserModel.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(termsRepository.findActiveRequiredByReferenceTime(any())).thenReturn(List.of());
                when(termsUserService.checkRequiredTerms(any(), any(), any())).thenReturn(true);
                when(statusRepository.findByNameIgnoreCase(any())).thenReturn(Optional.of(new StatusEntity(
                                UUID.randomUUID(),
                                "PENDENTE",
                                List.of())));
                when(userStatusRepository.save(any(UserStatusEntity.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(roleJpaRepository.findByNameIgnoreCase(any())).thenReturn(Optional.of(new RoleEntity(
                                UUID.randomUUID(),
                                "USER",
                                List.of())));

                AppUserEntity mockUser = AppUserEntity.builder()
                                .name("Maria Silva")
                                .email("maria@teste.com")
                                .password("encoded_SenhaFuerte123")
                                .createdAt(LocalDateTime.now())
                                .phone("11999998888")
                                .build();

                when(appUserRepository.saveAndFlush(any())).thenReturn(mockUser);

                UsuarioCadastroService service = new UsuarioCadastroService(
                                usuarioCadastroRepository,
                                appUserRepository,
                                termsUserService,
                                statusRepository,
                                userStatusRepository,
                                termsService,
                                userStatusService,
                                passwordEncoder,
                                jpaUsuarioCadastroRepository,
                                roleJpaRepository,
                                userStatusRepository,
                                userRepository,
                externalUserPrivacyRegistryService);

                UsuarioCadastroRequest request = new UsuarioCadastroRequest();
                request.setNomeCompleto("Maria Silva");
                request.setEmail("  MARIA@TESTE.COM ");
                request.setSenha("SenhaFuerte123");
                request.setTelefone(" 11999998888 ");
                request.setTermsIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

                AppUserEntity registeredUser = service.cadastrar(request);

                InOrder inOrder = inOrder(appUserRepository, userStatusRepository);
                inOrder.verify(appUserRepository).saveAndFlush(any(AppUserEntity.class));
                inOrder.verify(userStatusRepository).save(any(UserStatusEntity.class));

        assertEquals("maria@teste.com", registeredUser.getEmail());
        assertEquals("11999998888", registeredUser.getPhone());
        assertNotNull(registeredUser.getCreatedAt());
        assertNotNull(registeredUser.getPassword());
        assertNotEquals("SenhaFuerte123", registeredUser.getPassword());
        assertEquals("encoded_SenhaFuerte123", registeredUser.getPassword());
        verify(externalUserPrivacyRegistryService).upsertActiveUser(mockUser.getId(), "maria@teste.com");
    }

        @Test
        void deveRejeitarEmailDuplicado() {
                UsuarioCadastroRepository usuarioCadastroRepository = mock(UsuarioCadastroRepository.class);
                AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
                TermsUserService termsUserService = mock(TermsUserService.class);
                StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
                UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
                TermsService termsService = mock(TermsService.class);
                UserStatusService userStatusService = mock(UserStatusService.class);
                PasswordEncoder passwordEncoder = new MockPasswordEncoder();
                JpaUsuarioCadastroRepository jpaUsuarioCadastroRepository = mock(JpaUsuarioCadastroRepository.class);
                RoleJpaRepository roleJpaRepository = mock(RoleJpaRepository.class);
                UsuarioRepository userRepository = mock(UsuarioRepository.class);

        ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService =
                mock(ExternalUserPrivacyRegistryService.class);

                when(usuarioCadastroRepository.existsByEmail(any())).thenReturn(true);
                when(jpaUsuarioCadastroRepository.normalizarEmail(any())).thenReturn("duplicado@teste.com");
                when(termsUserService.checkRequiredTerms(any(), any(), any())).thenReturn(true);
                when(statusRepository.findByNameIgnoreCase(any())).thenReturn(Optional.of(new StatusEntity(
                                UUID.randomUUID(),
                                "PENDENTE",
                                List.of())));
                when(roleJpaRepository.findByNameIgnoreCase(any())).thenReturn(Optional.of(new RoleEntity(
                                UUID.randomUUID(),
                                "USER",
                                List.of())));

                UsuarioCadastroService service = new UsuarioCadastroService(
                                usuarioCadastroRepository,
                                appUserRepository,
                                termsUserService,
                                statusRepository,
                                userStatusRepository,
                                termsService,
                                userStatusService,
                                passwordEncoder,
                                jpaUsuarioCadastroRepository,
                                roleJpaRepository,
                                userStatusRepository,
                                userRepository,
                externalUserPrivacyRegistryService);

                UsuarioCadastroRequest request = new UsuarioCadastroRequest();
                request.setNomeCompleto("Joao");
                request.setEmail("DUPLICADO@TESTE.COM");
                request.setSenha("SenhaFuerte123");
                request.setTermsIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

                EmailJaCadastradoException exception = assertThrows(
                                EmailJaCadastradoException.class,
                                () -> service.cadastrar(request));

                assertEquals("This email address is already registered (duplicado@teste.com)", exception.getMessage());
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
