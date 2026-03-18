package com.energia.backend.repository;

import com.energia.backend.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LogRepository extends JpaRepository<SystemLog, Long> {
    

}