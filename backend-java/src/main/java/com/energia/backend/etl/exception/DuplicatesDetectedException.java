package com.energia.backend.etl.exception;

public class DuplicatesDetectedException extends RuntimeException {
    private final int count;
    private final String details;

    public DuplicatesDetectedException(int count, String details) {
        super("Duplicatas detectadas: " + count);
        this.count = count;
        this.details = details;
    }

    public int getCount() {
        return count;
    }

    public String getDetails() {
        return details;
    }
}
