package com.energia.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.UserTermsEntity;


public interface UserTermsRespository extends JpaRepository<UserTermsEntity, UUID>{
    
}
