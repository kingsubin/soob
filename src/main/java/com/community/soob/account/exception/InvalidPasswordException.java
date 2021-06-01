package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class InvalidPasswordException extends SoobException {
    public InvalidPasswordException() {
    }

    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
