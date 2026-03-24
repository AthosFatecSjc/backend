package com.energia.backend.service;

import com.energia.backend.model.AppUser;
import com.energia.backend.model.Terms;
import com.energia.backend.model.UserTerms;
import com.energia.backend.repository.AppUserRepository;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserTermsService {

    private final UserTermsRepository repository;
    private final AppUserRepository userRepository;
    private final TermsRepository termsRepository;

    public UserTermsService(UserTermsRepository repository,
                            AppUserRepository userRepository,
                            TermsRepository termsRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.termsRepository = termsRepository;
    }

    public UserTerms acceptTerms(UUID userId, UUID termsId, String acceptedFrom) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        // 🔥 Regra: não pode aceitar o mesmo termo duas vezes sem revogar
        boolean alreadyAccepted = repository
                .existsByUserIdAndTermsIdAndRevokedAtIsNull(userId, termsId);

        if (alreadyAccepted) {
            throw new RuntimeException("Termo já foi aceito pelo usuário");
        }

        UserTerms userTerms = UserTerms.builder()
                .user(user)
                .terms(terms)
                .acceptedAt(LocalDateTime.now())
                .acceptedFrom(acceptedFrom)
                .build();

        return repository.save(userTerms);
    }

    public void revokeTerms(UUID userId, UUID termsId) {

        List<UserTerms> termsList = repository.findByUserIdAndRevokedAtIsNull(userId);

        termsList.stream()
                .filter(t -> t.getTerms().getId().equals(termsId))
                .findFirst()
                .ifPresent(term -> {
                    term.setRevokedAt(LocalDateTime.now());
                    repository.save(term);
                });
    }

    public List<UserTerms> getUserTerms(UUID userId) {
        return repository.findByUserId(userId);
    }
}