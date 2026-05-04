package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energia.backend.model.TermsEntity;

public interface TermsRepository extends JpaRepository<TermsEntity, UUID> {

  @Query("""
          select t
          from TermsEntity t
          join fetch t.termType tt
          where tt.isRequired = true
            and t.effectivityStartAt <= :referenceTime
            and (t.effectivityEndAt is null or t.effectivityEndAt > :referenceTime)
          order by tt.name asc
      """)
  List<TermsEntity> findActiveRequiredByReferenceTime(
      @Param("referenceTime") LocalDateTime referenceTime);

  @Query("""
          select t
          from TermsEntity t
          join fetch t.termType tt
          where t.effectivityStartAt <= :referenceTime
            and (t.effectivityEndAt is null or t.effectivityEndAt > :referenceTime)
          order by tt.name asc
      """)
  List<TermsEntity> findActiveByReferenceTime(
      @Param("referenceTime") LocalDateTime referenceTime);

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

  @Query("""
          select t
          from TermsEntity t
          join fetch t.termType tt
          where upper(tt.name) = upper(:typeName)
            and t.effectivityStartAt <= :referenceTime
            and (t.effectivityEndAt is null or t.effectivityEndAt > :referenceTime)
          order by t.clause asc
      """)
  List<TermsEntity> findActivesByTypeName(
      @Param("typeName") String typeName,
      @Param("referenceTime") LocalDateTime referenceTime);

  @Modifying
  @Query("""
          update TermsEntity t
          set t.effectivityEndAt = :now
          where t.termType.id = :termTypeId
            and t.effectivityEndAt is null
      """)
  void closeCurrentTermsByType(
      @Param("termTypeId") UUID termTypeId,
      @Param("now") LocalDateTime now);

  @Query("""
      select coalesce(max(t.clause), 0)
      from TermsEntity t
      where t.termType.id = :termTypeId
      """)
  Integer findMaxClauseByTermType(@Param("termTypeId") UUID termTypeId);


  @Query("""
    select t
    from TermsEntity t
    where t.effectivityStartAt <= :cutoffDate
      and t.effectivityStartAt = (
          select max(t2.effectivityStartAt)
          from TermsEntity t2
          where t2.termType.id = t.termType.id
            and t2.clause = t.clause
            and t2.effectivityStartAt <= :cutoffDate
      )
      and not exists (
          select 1
          from UserTermsEntity ut
          where ut.user.id = :userId
            and ut.terms = t
            and ut.action = com.energia.backend.model.UserTermsAction.ACCEPTED
            and ut.actionAt = (
                select max(ut2.actionAt)
                from UserTermsEntity ut2
                where ut2.user.id = :userId
                  and ut2.terms = t
            )
      )
    """)
    List<TermsEntity> findPendingLatestTermsByUser(
            @Param("userId") UUID userId,
            @Param("cutoffDate") LocalDateTime cutoffDate
    );
}