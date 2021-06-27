package com.community.soob.common;

public class BusinessException extends RuntimeException {
    public BusinessException() { }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
