package com.energia.backend.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energia.backend.model.UserTermsEntity;


public interface UserTermsRespository extends JpaRepository<UserTermsEntity, UUID>{

    @Query("""
            select ut
            from UserTermsEntity ut
            join fetch ut.terms t
            join fetch t.termType tt
            where ut.user.id = :userId
            order by coalesce(ut.revokedAt, ut.acceptedAt) desc, t.createdAt desc
            """)
    List<UserTermsEntity> findHistoryByUserId(@Param("userId") UUID userId);
}
