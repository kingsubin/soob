package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class DuplicateEmailException extends SoobException {
    public DuplicateEmailException() {
        super();
    }

    public DuplicateEmailException(String message) {
        super(message);
    }
}
