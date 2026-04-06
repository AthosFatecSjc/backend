package com.energia.backend.repository;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.AnonymizationStatus;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.dto.Usuario;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Primary
public class JpaUsuarioCadastroRepository implements UsuarioCadastroRepository {
    private static final String DEFAULT_USER_ROLE = "user";

    private final AppUserJpaRepository appUserRepository;
    private final RoleJpaRepository roleRepository;
    private final StatusJpaRepository statusRepository;
    private final UserStatusJpaRepository userStatusRepository;

    public JpaUsuarioCadastroRepository(
            AppUserJpaRepository appUserRepository,
            RoleJpaRepository roleRepository,
            StatusJpaRepository statusRepository,
            UserStatusJpaRepository userStatusRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.statusRepository = statusRepository;
        this.userStatusRepository = userStatusRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return appUserRepository.existsByEmailIgnoreCase(normalizarEmail(email));
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        // Garantir que a role "user" existe
        RoleEntity userRole = roleRepository.findByNameIgnoreCase(DEFAULT_USER_ROLE)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(DEFAULT_USER_ROLE).build()));

        AppUserEntity entity = AppUserEntity.builder()
                .name(usuario.getNomeCompleto())
                .email(normalizarEmail(usuario.getEmail()))
                .password(usuario.getSenhaHash())
                .phone(usuario.getTelefone())
                .anonymizationStatus(AnonymizationStatus.ACTIVE)
                .roles(List.of(userRole))
                .build();

        AppUserEntity savedUser = appUserRepository.save(entity);

        StatusEntity statusPendente = statusRepository
                .findByNameIgnoreCase(StatusUsuario.PENDENTE.name())
                .orElseGet(() -> statusRepository.save(
                        StatusEntity.builder().name(StatusUsuario.PENDENTE.name()).build()
                ));

        UserStatusEntity userStatus = UserStatusEntity.builder()
                .user(savedUser)
                .status(statusPendente)
                .assignedAt(usuario.getDataCadastro() != null ? usuario.getDataCadastro() : LocalDateTime.now())
                .build();

        userStatusRepository.save(userStatus);
        return usuario;
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}

