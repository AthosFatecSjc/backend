package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energia.backend.model.TermsEntity;

public interface TermsRepository extends JpaRepository<TermsEntity, UUID> {
  // retorna todos os termos ativos e obrigatórios.
  @Query("""
        select t
        from TermsEntity t
        join fetch t.termType tt
        where tt.isRequired = true
          and t.effectivityStartAt <= :referenceTime
          and not exists (
              select 1
              from TermsEntity t2
              where t2.termType = t.termType
                and t2.version > t.version
                and t2.effectivityStartAt <= :referenceTime
          )
        order by tt.name asc
      """)
  List<TermsEntity> findActiveRequiredByReferenceTime(@Param("referenceTime") LocalDateTime referenceTime);

  // retorna todos os termos ativos, independentemente de serem obrigatórios ou
  // não.
  @Query("""
        select t
        from TermsEntity t
        join fetch t.termType tt
        where t.effectivityStartAt <= :referenceTime
          and not exists (
              select 1
              from TermsEntity t2
              where t2.termType = t.termType
                and t2.version > t.version
                and t2.effectivityStartAt <= :referenceTime
          )
        order by tt.name asc
      """)
  List<TermsEntity> findActiveByReferenceTime(@Param("referenceTime") LocalDateTime referenceTime);

  @Query("""
    select t
    from TermsEntity t
    join fetch t.termType tt
    where tt.name in :names
    """)
  List<TermsEntity> findByTermTypeNames(@Param("names") Set<String> names);

  @Query("""
      select t
      from TermsEntity t
      join fetch t.termType tt
      where t.id in :ids
      """)
  List<TermsEntity> findAllWithTypeByIdIn(@Param("ids") List<UUID> ids);
}