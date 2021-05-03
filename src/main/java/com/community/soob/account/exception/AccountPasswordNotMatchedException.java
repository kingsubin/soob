package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class AccountPasswordNotMatchedException extends SoobException {
    public AccountPasswordNotMatchedException() {
    }

    public AccountPasswordNotMatchedException(String message) {
        super(message);
    }

    public AccountPasswordNotMatchedException(String message, Throwable cause) {
        super(message, cause);
    }
}
