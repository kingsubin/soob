package com.community.soob.post.exception;

import com.community.soob.config.SoobException;

public class PostNotFoundException extends SoobException {
    public PostNotFoundException() {
        super();
    }

    public PostNotFoundException(String message) {
        super(message);
    }
}
