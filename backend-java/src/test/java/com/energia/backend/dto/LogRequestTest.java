package com.energia.backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogRequestTest {
    @Test
    void testMetadataSizeValidation() {
        LogRequest.LogRequestBuilder builder = LogRequest.builder()
                .actor("actor")
                .sourceType(null)
                .event(null)
                .result(null)
                .logCategory(null)
                .description("desc")
                .metadata("a".repeat(4001))
                .targetRef("target")
                .module("mod");
        LogRequest req = builder.build();
        assertTrue(req.getMetadata().length() > 4000);
    }
}
