package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class AccountNotFoundException extends SoobException {
    public AccountNotFoundException() {
        super();
    }

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
