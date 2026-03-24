package com.energia.backend.service;

import com.energia.backend.model.Terms;
import com.energia.backend.repository.TermsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TermsService {

    private final TermsRepository repository;

    public TermsService(TermsRepository repository) {
        this.repository = repository;
    }

    public Terms create(Terms terms) {
        if (terms.getCreatedAt() == null) {
            terms.setCreatedAt(LocalDateTime.now());
    }
    return repository.save(terms);
}

    public List<Terms> findByType(UUID termTypeId) {
        return repository.findByTermTypeId(termTypeId);
    }

    public List<Terms> findActiveTerms() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByEffectivityStartAtBeforeAndEffectivityEndAtAfter(now, now);
    }
}