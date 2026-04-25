package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.UserTermsEntity;


public interface UserTermsRepository extends JpaRepository<UserTermsEntity, UUID>{
    List<UserTermsEntity> findByUserAndActionAtLessThanEqual(
            AppUserEntity user,
            LocalDateTime referenceTime
    );    

    List<UserTermsEntity> findByUserAndTermsOrderByActionAtDesc(
        AppUserEntity user,
        TermsEntity terms
    );

    @Query("""
        select ut
        from UserTermsEntity ut
        join fetch ut.terms t
        where ut.user.id = :userId
        order by ut.actionAt desc
    """)
    List<UserTermsEntity> findHistoryByUserId(@Param("userId") UUID userId);

    Optional<UserTermsEntity> findTopByUserAndTermsIdOrderByActionAtDesc(
        AppUserEntity user,
        UUID termsId
    );
}
