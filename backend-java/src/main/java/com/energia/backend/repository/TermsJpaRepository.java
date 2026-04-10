package com.energia.backend.repository;

import com.energia.backend.model.TermsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TermsJpaRepository extends JpaRepository<TermsEntity, UUID> {

    @Query("""
            select t
            from TermsEntity t
            join fetch t.termType tt
            where upper(tt.name) = upper(:typeName)
              and t.effectivityStartAt <= :referenceTime
              and (t.effectivityEndAt is null or t.effectivityEndAt > :referenceTime)
            order by t.version desc, t.createdAt desc
            """)
    List<TermsEntity> findActiveByTypeName(
            @Param("typeName") String typeName,
            @Param("referenceTime") LocalDateTime referenceTime
    );
}
