package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.energia.backend.model.TermsEntity;

public record TermosResponse(
        UUID termId,
        String typeName,
        Boolean required,
        String content, 
        Integer clause,
        LocalDateTime effectivityStartAt,
        LocalDateTime effectivityEndAt
) {
        public static TermosResponse fromEntity(
                TermsEntity entity
        ) {
                return new TermosResponse(
                        entity.getId(),
                        entity.getTermType().getName().name(),
                        entity.getTermType().getIsRequired(),
                        entity.getContent(), 
                        entity.getClause(),
                        entity.getEffectivityStartAt(),
                        entity.getEffectivityEndAt()
                );
        }

}