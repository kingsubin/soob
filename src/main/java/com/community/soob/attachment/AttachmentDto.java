package com.community.soob.attachment;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AttachmentDto {
    private String fileName;
    private String hashedFileName;
    private String filePath;

    @Builder
    public AttachmentDto(String fileName, String hashedFileName, String filePath) {
        this.fileName = fileName;
        this.hashedFileName = hashedFileName;
        this.filePath = filePath;
    }

    public Attachment toEntity() {
        return Attachment.builder()
                .fileName(fileName)
                .hashedFileName(hashedFileName)
                .filePath(filePath)
                .build();
    }

    public static AttachmentDto fromEntity(Attachment attachment) {
        return AttachmentDto.builder()
                .fileName(attachment.getFileName())
                .hashedFileName(attachment.getHashedFileName())
                .fileName(attachment.getFileName())
                .build();
    }
}
