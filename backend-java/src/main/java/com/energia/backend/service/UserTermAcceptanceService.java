package com.energia.backend.service;

import com.energia.backend.DTO.UserTermAcceptanceRequestDTO;
import com.energia.backend.DTO.UserTermAcceptanceResponseDTO;
import com.energia.backend.model.TermEntity;
import com.energia.backend.model.UserEntity;
import com.energia.backend.model.UserTermAcceptance;
import com.energia.backend.repository.UserRepository;
import com.energia.backend.repository.UserTermAcceptanceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserTermAcceptanceService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserTermAcceptanceRepository repository;
    private final UserRepository userRepository;

    public UserTermAcceptanceService(UserTermAcceptanceRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserTermAcceptanceResponseDTO acceptTerm(UserTermAcceptanceRequestDTO dto) {

        TermEntity term = entityManager.find(TermEntity.class, dto.getTermId());
        if (term == null) {
            throw new RuntimeException("Term not found");
        }

        UserEntity user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId()).orElse(null);
        }

        if (user == null) {
            if (!Boolean.TRUE.equals(dto.getAccepted())) {
                throw new RuntimeException("User must exist or accept mandatory term to be created");
            }

            if (!Boolean.TRUE.equals(term.getMandatory())) {
                throw new RuntimeException("User must be created via signup process for non-mandatory terms");
            }

            validateUserCreationInfo(dto);
            user = createUserWithHash(dto);
        }

        Optional<UserTermAcceptance> existent = repository.findByUserIdAndTermId(user.getId(), dto.getTermId());

        UserTermAcceptance entity = existent.orElseGet(UserTermAcceptance::new);
        entity.setUser(user);
        entity.setTerm(term);
        entity.setAccepted(dto.getAccepted());
        entity.setAcceptedAt(Boolean.TRUE.equals(dto.getAccepted()) ? LocalDateTime.now() : null);

        UserTermAcceptance saved = repository.save(entity);

        UserTermAcceptanceResponseDTO response = new UserTermAcceptanceResponseDTO();
        response.setId(saved.getId());
        response.setUserId(saved.getUser().getId());
        response.setTermId(saved.getTerm().getId());
        response.setAccepted(saved.getAccepted());
        response.setAcceptedAt(saved.getAcceptedAt());

        return response;
    }

    private void validateUserCreationInfo(UserTermAcceptanceRequestDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("Name is required to create a new user");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email is required to create a new user");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Password is required to create a new user");
        }
        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already in use");
        });
    }

    private UserEntity createUserWithHash(UserTermAcceptanceRequestDTO dto) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserEntity newUser = UserEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .adminPermission(false)
                .build();
        return userRepository.save(newUser);
    }
}