package com.community.soob.attachment;

import com.community.soob.config.SoobException;

public class AttachmentException extends SoobException {
    public AttachmentException() {
    }

    public AttachmentException(String message) {
        super(message);
    }

    public AttachmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
