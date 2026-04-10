package com.energia.backend.repository;

import com.energia.backend.model.log.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long>,
        JpaSpecificationExecutor<SystemLog> {

}