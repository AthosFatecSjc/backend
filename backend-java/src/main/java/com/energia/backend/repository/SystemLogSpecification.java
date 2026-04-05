package com.energia.backend.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.energia.backend.dto.LogFilterRequest;
import com.energia.backend.model.log.SystemLog;

import jakarta.persistence.criteria.Predicate;

public class SystemLogSpecification {

    public static Specification<SystemLog> withFilters(LogFilterRequest filter) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getEvent() != null) {
                predicates.add(cb.equal(root.get("event"), filter.getEvent()));
            }

            if (filter.getResult() != null) {
                predicates.add(cb.equal(root.get("result"), filter.getResult()));
            }

            if (filter.getStartDate() != null && filter.getEndDate() != null) {
                predicates.add(cb.between(
                        root.<LocalDateTime>get("createdAt"),
                        filter.getStartDate(),
                        filter.getEndDate()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}