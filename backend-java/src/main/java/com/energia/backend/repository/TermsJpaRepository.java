package com.energia.backend.repository;

import com.energia.backend.model.TermsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TermsJpaRepository extends JpaRepository<TermsEntity, UUID> {
// retorna o termo do tipo especificado, que estivessee ativo no referenceTime.
        @Query("""
                select t
                from TermsEntity t
                join fetch t.termType tt
                where upper(tt.name) = upper(:typeName)
                  and t.effectivityStartAt <= :referenceTime
                  and not exists (
                      select 1
                      from TermsEntity t2
                      where t2.termType = t.termType
                        and t2.version > t.version
                        and t2.effectivityStartAt <= :referenceTime
                  )
        """)
        Optional<TermsEntity> findActiveByTypeName(
                @Param("typeName") String typeName,
                @Param("referenceTime") LocalDateTime referenceTime
        );

        @Query("""
                select coalesce(max(t.version), 0)
                from TermsEntity t
                where t.termType.id = :termTypeId
            """)
            Integer findMaxVersionByTermType(@Param("termTypeId") UUID termTypeId);

        @Modifying
        @Query("""
            update TermsEntity t
            set t.isActive = false
            where t.termType.id = :termTypeId
        """)
        void deactivateByType(@Param("termTypeId") UUID termTypeId);
}
