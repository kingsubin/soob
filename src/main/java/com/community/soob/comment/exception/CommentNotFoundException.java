package com.community.soob.comment.exception;

import com.community.soob.config.SoobException;

public class CommentNotFoundException extends SoobException {
    public CommentNotFoundException() { }

    public CommentNotFoundException(String message) {
        super(message);
    }
}
