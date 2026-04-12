package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.UserTermsEntity;


public interface UserTermsRespository extends JpaRepository<UserTermsEntity, UUID>{
    List<UserTermsEntity> findByUserAndActionAtLessThanEqual(
            AppUserEntity user,
            LocalDateTime referenceTime
    );    

    List<UserTermsEntity> findByUserAndTermsOrderByActionAtDesc(
        AppUserEntity user,
        TermsEntity terms
    );
}
