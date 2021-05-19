package com.community.soob.comment.controller.dto;

import com.community.soob.comment.domain.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private Long authorId;
    private Long postId;
    private String author;
    private String content;
    private int heartCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    @Builder
    public CommentResponseDto(Long id, Long authorId, Long postId, String author, String content, int heartCount, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        this.id = id;
        this.authorId = authorId;
        this.postId = postId;
        this.author = author;
        this.content = content;
        this.heartCount = heartCount;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static CommentResponseDto fromEntity(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .authorId(comment.getAuthor().getId())
                .postId(comment.getPost().getId())
                .author(comment.getAuthor().getNickname())
                .content(comment.getContent())
                .heartCount(comment.getHeartCount())
                .createdAt(comment.getCreatedAt())
                .lastModifiedAt(comment.getLastModifiedAt())
                .build();
    }
}
