package com.energia.backend.dto;

import com.energia.backend.model.LogEvent;
import com.energia.backend.model.ResultType;
import com.energia.backend.model.SourceType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogRequest {

    private String actor;
    private SourceType sourceType;
    private LogEvent event;
    private ResultType result;

    private String description;
    private String metadata;

    private String targetRef;
    private String module;
}

