package com.energia.backend.dto;

import java.time.LocalDateTime;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;

import lombok.Data;

@Data
public class LogFilterRequest {

    private LogEvent event;
    private ResultType result;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}