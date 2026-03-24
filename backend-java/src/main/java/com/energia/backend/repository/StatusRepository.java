package com.energia.backend.repository;

import com.energia.backend.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatusRepository extends JpaRepository<Status, UUID> {

    Optional<Status> findByName(String name);
}