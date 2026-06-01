package com.energia.backend.dto;

public record BulkEmailResponse(
        int recipientsFound,
        int sentCount,
        int failedCount,
        String message
) {
}
