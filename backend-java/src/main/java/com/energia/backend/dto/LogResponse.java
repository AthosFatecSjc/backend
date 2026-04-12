package com.energia.backend.dto;

import java.time.LocalDateTime;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogResponse {

    private Long id;
    private LocalDateTime createdAt;
    private String actorRef;
    private String targetRef;
    private SourceType sourceType;
    private LogEvent event;
    private ResultType result;
    private LogCategory logCategory;
    private String description;
    private String createdByModule;
    private String metadata;

}
