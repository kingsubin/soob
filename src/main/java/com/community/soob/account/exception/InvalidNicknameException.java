package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class InvalidNicknameException extends SoobException {
    public InvalidNicknameException() {
    }

    public InvalidNicknameException(String message) {
        super(message);
    }

    public InvalidNicknameException(String message, Throwable cause) {
        super(message, cause);
    }
}
