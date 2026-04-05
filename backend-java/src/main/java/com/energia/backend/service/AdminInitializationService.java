package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.model.AnonymizationStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.RoleJpaRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;

/**
 * Serviço de bootstrap seguro para criar o primeiro usuário administrador.
 * Lê credenciais de variáveis de ambiente, não de hardcode ou migrations versionadas.
 *
 * Variáveis esperadas:
 * - ADMIN_EMAIL (obrigatória para ativar)
 * - ADMIN_PASSWORD (obrigatória para ativar)
 *
 * Executa automaticamente na inicialização da aplicação.
 */
@Service
public class AdminInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializationService.class);

    @Value("${admin.email:#{null}}")
    private String adminEmail;

    @Value("${admin.password:#{null}}")
    private String adminPassword;

    private final AppUserJpaRepository appUserRepository;
    private final RoleJpaRepository roleRepository;
    private final StatusJpaRepository statusRepository;
    private final UserStatusJpaRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializationService(
            AppUserJpaRepository appUserRepository,
            RoleJpaRepository roleRepository,
            StatusJpaRepository statusRepository,
            UserStatusJpaRepository userStatusRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.statusRepository = statusRepository;
        this.userStatusRepository = userStatusRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Se as variáveis de ambiente não foram configuradas, não cria admin
        if (adminEmail == null || adminEmail.isEmpty() || adminPassword == null || adminPassword.isEmpty()) {
            logger.info("Admin initialization disabled: ADMIN_EMAIL or ADMIN_PASSWORD not set");
            return;
        }

        // Normalizar email
        String normalizedEmail = adminEmail.trim().toLowerCase();

        // Verificar se o admin já existe
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            logger.info("Admin user already exists with email: {}", normalizedEmail);
            return;
        }

        try {
            logger.info("Creating admin user with email: {}", normalizedEmail);

            // Garantir que a role ADMIN existe
            RoleEntity adminRole = roleRepository.findByNameIgnoreCase("admin")
                    .orElseGet(() -> {
                        RoleEntity newRole = RoleEntity.builder().name("admin").build();
                        return roleRepository.save(newRole);
                    });

            // Garantir que o status ATIVO existe
            StatusEntity ativoStatus = statusRepository.findByNameIgnoreCase("ATIVO")
                    .orElseGet(() -> {
                        StatusEntity newStatus = StatusEntity.builder().name("ATIVO").build();
                        return statusRepository.save(newStatus);
                    });

            // Hash da senha com BCrypt
            String encodedPassword = passwordEncoder.encode(adminPassword);

            // Criar usuário admin
            AppUserEntity adminUser = AppUserEntity.builder()
                    .id(UUID.randomUUID())
                    .name("Administrator")
                    .email(normalizedEmail)
                    .password(encodedPassword)
                    .phone(null)
                    .anonymizationStatus(AnonymizationStatus.ACTIVE)
                    .roles(List.of(adminRole))
                    .build();

            AppUserEntity savedAdmin = appUserRepository.save(adminUser);

            // Atribuir status ATIVO ao admin
            UserStatusEntity adminStatus = UserStatusEntity.builder()
                    .id(UUID.randomUUID())
                    .user(savedAdmin)
                    .status(ativoStatus)
                    .assignedAt(LocalDateTime.now())
                    .rationaleForRejection(null)
                    .build();

            userStatusRepository.save(adminStatus);

            logger.info("Admin user created successfully with email: {}", normalizedEmail);
        } catch (Exception ex) {
            logger.error("Failed to create admin user", ex);
            throw new RuntimeException("Admin initialization failed", ex);
        }
    }
}
