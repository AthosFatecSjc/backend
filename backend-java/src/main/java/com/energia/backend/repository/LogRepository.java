package com.energia.backend.repository;

import com.energia.backend.model.Logger;


import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogRepository extends JpaRepository<Logger, Long> {
    
    Page<Logger> findByIsAuditavelTrue(Pageable pageable);
    Page<Logger> findByIsAuditavelFalse(Pageable pageable);
}