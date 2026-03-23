package com.energia.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energia.backend.model.log.SystemLog;


public interface LogRepository extends JpaRepository<SystemLog, Long> {
    

}