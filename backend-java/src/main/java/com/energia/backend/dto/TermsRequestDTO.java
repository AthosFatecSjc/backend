package com.energia.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TermsRequestDTO {

    private UUID termTypeId;
    private Integer version;
    private LocalDateTime effectivityStartAt;
    private LocalDateTime effectivityEndAt;
    private String content;
    private String description;
}