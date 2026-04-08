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

            if (filter.getActorRef() != null && !filter.getActorRef().isEmpty()) {
                predicates.add(cb.equal(root.get("actorRef"), filter.getActorRef()));
            }

            if (filter.getTargetRef() != null && !filter.getTargetRef().isEmpty()) {
                predicates.add(cb.equal(root.get("targetRef"), filter.getTargetRef()));
            }

            if (filter.getSourceType() != null) {
                predicates.add(cb.equal(root.get("sourceType"), filter.getSourceType()));
            }

            if (filter.getEvent() != null) {
                predicates.add(cb.equal(root.get("event"), filter.getEvent()));
            }

            if (filter.getResult() != null) {
                predicates.add(cb.equal(root.get("result"), filter.getResult()));
            }

            if (filter.getLogCategory() != null) {
                predicates.add(cb.equal(root.get("logCategory"), filter.getLogCategory()));
            }

            if (filter.getCreatedByModule() != null && !filter.getCreatedByModule().isEmpty()) {
                predicates.add(cb.equal(root.get("createdByModule"), filter.getCreatedByModule()));
            }

            if (filter.getDescription() != null && !filter.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), 
                    "%" + filter.getDescription().toLowerCase() + "%"));
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