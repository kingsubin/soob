package com.community.soob.account.exception;

import com.community.soob.config.SoobException;

public class DuplicateNicknameException extends SoobException {
    public DuplicateNicknameException() {
        super();
    }

    public DuplicateNicknameException(String message) {
        super(message);
    }
}
