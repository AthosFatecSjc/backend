package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.energia.backend.model.LoginSharingRequestEntity;
import com.energia.backend.model.LoginSharingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSharingPublicStatusDto {

    private UUID requestId;
    private String externalAgentName;
    private String externalAgentEmail;
    private LoginSharingStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;
    private String reason;

    public static LoginSharingPublicStatusDto fromEntity(LoginSharingRequestEntity entity) {
        return LoginSharingPublicStatusDto.builder()
                .requestId(entity.getId())
                .externalAgentName(entity.getExternalAgentName())
                .externalAgentEmail(entity.getExternalAgentEmail())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .respondedAt(entity.getRespondedAt())
                .expiresAt(entity.getExpiresAt())
                .reason(entity.getReason())
                .build();
    }
}