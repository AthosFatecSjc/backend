package com.energia.backend.service;

import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Component;

@Component
public class LogMetadataSanitizer {
    private static final int MAX_METADATA_SIZE = 2048;
    private static final String MASK = "[MASKED]";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern
            .compile("(?<!\\w)(\\+?\\d{1,3}[\\s-]?)?(\\(?\\d{2,3}\\)?[\\s-]?)?\\d{4,5}[\\s-]?\\d{4}(?!\\w)");
    private static final Pattern PASSWORD_PATTERN = Pattern
            .compile("(?i)\\b(senha|password|pwd)\\b['\"]?\\s*[:=]\\s*['\"]?[^,'\"} ]+");
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?i)(?<!\\w)(token|jwt|bearer)(?!\\w)['\"]?\\s*(?:[:=]\\s*)?['\"]?[^,'\"} ]+");
    private static final Pattern AUTH_HEADER_PATTERN = Pattern
            .compile("(?i)authorization['\"]?\\s*[:=]\\s*['\"]?[^,'\"} ]+");
    private static final Pattern COOKIE_PATTERN = Pattern.compile("(?i)cookie['\"]?\\s*[:=]\\s*['\"]?[^,'\"} ]+");
    private static final Pattern SECRET_PATTERN = Pattern
            .compile("(?i)secret|credential|api[_-]?key|chave[_-]?secreta|token");

    public Map<String, Object> sanitize(Map<String, Object> metadata) {
        if (metadata == null)
            return null;
        Map<String, Object> sanitized = new HashMap<>();
        int totalSize = 0;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String sanitizedValue = sanitizeValue(key, value);
            totalSize += sanitizedValue.length();
            if (totalSize > MAX_METADATA_SIZE) {
                sanitized.put("_truncated", "metadata too large, truncated");
                break;
            }
            sanitized.put(key, sanitizedValue);
        }
        return sanitized;
    }

    private String sanitizeValue(String key, Object value) {
        if (value == null)
            return null;
        String str = value.toString().strip();
        if (key.toLowerCase().contains("payload") || key.toLowerCase().contains("request")
                || key.toLowerCase().contains("response")) {
            return MASK;
        }
        str = EMAIL_PATTERN.matcher(str).replaceAll(MASK);
        str = PHONE_PATTERN.matcher(str).replaceAll(MASK);
        str = PASSWORD_PATTERN.matcher(str).replaceAll(MASK);
        str = TOKEN_PATTERN.matcher(str).replaceAll(MASK);
        str = AUTH_HEADER_PATTERN.matcher(str).replaceAll(MASK);
        str = COOKIE_PATTERN.matcher(str).replaceAll(MASK);
        str = SECRET_PATTERN.matcher(str).replaceAll(MASK);
        if (SECRET_PATTERN.matcher(key).find()) {
            return MASK;
        }
        if (value instanceof Map || value instanceof Iterable) {
            return MASK;
        }
        if (str.length() > 502) {
            return str.substring(0, 502) + "_truncated";
        }
        return str;
    }
}
