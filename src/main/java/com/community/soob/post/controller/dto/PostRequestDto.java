package com.community.soob.post.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PostRequestDto {
    @NotBlank(message = "제목을 작성해주세요.")
    private String title;

    @NotBlank(message = "내용을 작성해주세요.")
    private String content;
}
