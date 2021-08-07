package com.community.soob.attachment;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AttachmentDto {
    private Long id;
    private String fileName;
    private String filePath;

    @Builder
    public AttachmentDto(Long id, String fileName, String filePath) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public Attachment toEntity() {
        return Attachment.builder()
                .id(id)
                .fileName(fileName)
                .filePath(filePath)
                .build();
    }

    public static AttachmentDto fromEntity(Attachment attachment) {
        return AttachmentDto.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .build();
    }
}
