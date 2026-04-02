package com.energia.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.TermsEntity;


public interface TermsRepository extends JpaRepository<TermsEntity, UUID> {
    List<TermsEntity> findByIsRequiredTrue();

}
