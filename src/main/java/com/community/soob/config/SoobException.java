package com.community.soob.config;

public class SoobException extends RuntimeException {
    public SoobException() { }

    public SoobException(String message) {
        super(message);
    }

    public SoobException(String message, Throwable cause) {
        super(message, cause);
    }
}
