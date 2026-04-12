package com.energia.backend.dto;

import java.time.LocalDateTime;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;

import lombok.Data;

@Data
public class LogFilterRequest {

    private String actorRef;
    private String targetRef;
    private SourceType sourceType;
    private LogEvent event;
    private ResultType result;
    private LogCategory logCategory;
    private String createdByModule;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;

}