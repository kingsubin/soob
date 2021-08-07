package com.community.soob.post.controller.dto;

import com.community.soob.attachment.Attachment;
import com.community.soob.post.domain.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponseDto {
    private Long id;
    private Long boardId;
    private String boardName;
    private String author;
    private String title;
    private String content;
    private List<Attachment> attachments;
    private int readCount;
    private int heartCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    @Builder
    public PostResponseDto(Long id, Long boardId, String boardName, String author, String title, String content, List<Attachment> attachments, int readCount, int heartCount, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        this.id = id;
        this.boardId = boardId;
        this.boardName = boardName;
        this.author = author;
        this.title = title;
        this.content = content;
        this.attachments = attachments;
        this.readCount = readCount;
        this.heartCount = heartCount;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static PostResponseDto fromEntity(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .boardId(post.getBoard().getId())
                .boardName(post.getBoard().getName())
                .author(post.getAuthor().getNickname())
                .title(post.getTitle())
                .content(post.getContent())
                .attachments(post.getAttachments())
                .readCount(post.getReadCount())
                .heartCount(post.getHeartCount())
                .createdAt(post.getCreatedAt())
                .lastModifiedAt(post.getLastModifiedAt())
                .build();
    }
}
