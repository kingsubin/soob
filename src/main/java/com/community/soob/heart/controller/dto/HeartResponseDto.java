package com.community.soob.heart.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class HeartResponseDto {
    private Long accountId;
    private Long postId;
    private Long commentId;

    @Builder
    public HeartResponseDto(Long accountId, Long postId, Long commentId) {
        this.accountId = accountId;
        this.postId = postId;
        this.commentId = commentId;
    }
}
