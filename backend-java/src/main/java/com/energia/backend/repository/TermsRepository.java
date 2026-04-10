package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energia.backend.model.TermsEntity;

public interface TermsRepository extends JpaRepository<TermsEntity, UUID> {
    @Query("""
            select t
            from TermsEntity t
            join fetch t.termType tt
            where t.isRequired = true
              and t.effectivityStartAt <= :referenceTime
              and (t.effectivityEndAt is null or t.effectivityEndAt > :referenceTime)
            order by tt.name asc, t.version desc, t.createdAt desc
            """)
    List<TermsEntity> findActiveRequiredByReferenceTime(@Param("referenceTime") LocalDateTime referenceTime);
}
