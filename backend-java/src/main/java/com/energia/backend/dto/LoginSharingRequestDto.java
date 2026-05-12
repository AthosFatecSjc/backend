package com.energia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSharingRequestDto {
    
    @NotBlank(message = "External agent name is required")
    private String externalAgentName;
    
    @NotBlank(message = "External agent email is required")
    @Email(message = "Invalid email format for external agent")
    private String externalAgentEmail;
    
    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format for user")
    private String userEmail;
}
