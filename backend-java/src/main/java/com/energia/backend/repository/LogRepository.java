package com.energia.backend.repository;

import com.energia.backend.model.Logger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface LogRepository extends JpaRepository<Logger, Long> {
    
    List<Logger> findByIsAuditavelTrue();
    List<Logger> findByIsAuditavelFalse();
}