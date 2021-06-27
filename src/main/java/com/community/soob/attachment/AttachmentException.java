package com.community.soob.attachment;

import com.community.soob.common.BusinessException;

public class AttachmentException extends BusinessException {
    public AttachmentException() { }

    public AttachmentException(String message) {
        super(message);
    }

    public AttachmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
