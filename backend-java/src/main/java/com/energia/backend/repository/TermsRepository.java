package com.energia.backend.repository;

import com.energia.backend.model.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TermsRepository extends JpaRepository<Terms, UUID> {

    List<Terms> findByTermTypeId(UUID termTypeId);

    List<Terms> findByTermTypeIdAndVersion(UUID termTypeId, Integer version);

    List<Terms> findByEffectivityStartAtBeforeAndEffectivityEndAtAfter(
            LocalDateTime now1,
            LocalDateTime now2
    );
}