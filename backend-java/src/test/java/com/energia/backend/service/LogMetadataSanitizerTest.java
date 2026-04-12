package com.energia.backend.service;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogMetadataSanitizerTest {
    private final LogMetadataSanitizer sanitizer = new LogMetadataSanitizer();

    @Test
    void testValidSmallMetadata() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("info", "ok");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("ok", sanitized.get("info"));
    }

    @Test
    void testEmailIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("email", "user@email.com");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("email"));
    }

    @Test
    void testPhoneIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("phone", "+55 11 91234-5678");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("phone"));
    }

    @Test
    void testPasswordIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("senha", "senha=123456");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("senha"));
    }

    @Test
    void testTokenIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("token", "token=abcdef");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("token"));
    }

    @Test
    void testAuthorizationHeaderIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("Authorization", "Bearer abcdef");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("Authorization"));
    }

    @Test
    void testCookieIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("cookie", "cookie=abc");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("cookie"));
    }

    @Test
    void testPayloadIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("payload", "raw request");
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("payload"));
    }

    @Test
    void testOversizedMetadataIsTruncated() {
        Map<String, Object> meta = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3000; i++) sb.append("a");
        meta.put("big", sb.toString());
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertTrue(sanitized.containsKey("_truncated"));
    }

    @Test
    void testNestedObjectIsMasked() {
        Map<String, Object> meta = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("foo", "bar");
        meta.put("nested", nested);
        Map<String, Object> sanitized = sanitizer.sanitize(meta);
        assertEquals("[MASKED]", sanitized.get("nested"));
    }
}
