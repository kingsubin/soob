package com.community.soob.post.exception;

import com.community.soob.config.SoobException;

public class AuthorNotEqualException extends SoobException {
    public AuthorNotEqualException() {
        super();
    }

    public AuthorNotEqualException(String message) {
        super(message);
    }
}
