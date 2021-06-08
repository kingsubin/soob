package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class InvalidEmailException extends SoobException {
    public InvalidEmailException() {
    }

    public InvalidEmailException(String message) {
        super(message);
    }

    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
