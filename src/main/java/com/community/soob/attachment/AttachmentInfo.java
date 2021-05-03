package com.community.soob.attachment;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class AttachmentInfo {
    private String originalFileName;
    private String contentType;
    private byte[] contents;

    public AttachmentInfo(String originalFileName, String contentType, byte[] contents) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.contents = contents;
    }
}
