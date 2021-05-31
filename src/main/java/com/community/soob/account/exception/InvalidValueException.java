package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class InvalidValueException extends SoobException {
    public InvalidValueException() {
    }

    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
