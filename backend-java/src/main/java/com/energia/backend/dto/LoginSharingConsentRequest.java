package com.energia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSharingConsentRequest {
    
    @NotNull(message = "Approval status is required")
    private Boolean approved;
    
    private String reason;
}
