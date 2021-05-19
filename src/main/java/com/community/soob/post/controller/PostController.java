package com.community.soob.post.controller;

import com.community.soob.account.config.CurrentAccount;
import com.community.soob.account.domain.Account;
import com.community.soob.attachment.Attachment;
import com.community.soob.attachment.AttachmentException;
import com.community.soob.attachment.AttachmentInfo;
import com.community.soob.attachment.AttachmentService;
import com.community.soob.post.controller.dto.PostRequestDto;
import com.community.soob.post.controller.dto.PostResponseDto;
import com.community.soob.post.domain.Post;
import com.community.soob.post.exception.AuthorNotEqualException;
import com.community.soob.post.service.PostService;
import com.community.soob.response.ResultResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Api(tags = {"3, Post"})
@RequestMapping("/api/v1/boards/{boardId}/posts")
@RequiredArgsConstructor
@Controller
public class PostController {
    private final PostService postService;
    private final AttachmentService attachmentService;

    @ApiOperation(value = "게시글 작성", notes = "회원이 게시글을 작성한다.")
    @PostMapping
    public ResultResponse<Void> savePost(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시판 번호", required = true) @PathVariable Long boardId,
            @ApiParam(value = "게시글작성DTO", required = true) @Valid @RequestBody final PostRequestDto saveRequestDto,
            @ApiParam(value = "게시글이미지") @RequestPart(name = "files") List<MultipartFile> files) {
        Post savedPost = postService.createPost(account, boardId, saveRequestDto.getTitle(), saveRequestDto.getContent());

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                AttachmentInfo attachmentInfo = null;
                try {
                    attachmentInfo = new AttachmentInfo(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getBytes()
                    );
                } catch (IOException e) {
                    throw new AttachmentException();
                }

                Attachment attachment = attachmentService.savePostImage(attachmentInfo);
                attachment.setPost(savedPost);
                savedPost.addAttachment(attachment);
            }
        }

        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "게시글 단건 조회", notes = "게시글번호로 게시글을 조회한다.")
    @GetMapping("/{postId}")
    public ResultResponse<PostResponseDto> getPost(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시판번호", required = true) @PathVariable Long boardId,
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId) {
        PostResponseDto responseDto = PostResponseDto.fromEntity(postService.getPost(postId));
        return ResultResponse.of(ResultResponse.SUCCESS, responseDto);
    }

    @ApiOperation(value = "게시글 수정", notes = "게시글번호로 게시글을 수정한다.")
    @PutMapping("/{postId}")
    public ResultResponse<PostResponseDto> updatePost(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시판번호", required = true) @PathVariable Long boardId,
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId,
            @ApiParam(value = "게시글수정DTO", required = true) @Valid @RequestBody final PostRequestDto updateRequestDto,
            @ApiParam(value = "이미지") @RequestParam(name = "files") List<MultipartFile> files) {
        // 작성자 일치하는지
        boolean authorMatched = postService.isAuthorMatched(account, postId);
        if (!authorMatched) {
            throw new AuthorNotEqualException();
        }
        Post post = postService.getPost(postId);

        // 새 이미지가 존재한다면
        if (files != null && !files.isEmpty()) {
            // 기존 이미지 삭제
            for (Attachment attachment : post.getAttachments()) {
                post.removeAttachment(attachment);
                attachmentService.deleteAttachment(attachment.getId());
            }

            // 새 이미지 저장
            for (MultipartFile file : files) {
                AttachmentInfo attachmentInfo = null;
                try {
                    attachmentInfo = new AttachmentInfo(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getBytes()
                    );
                } catch (IOException e) {
                    throw new AttachmentException();
                }

                Attachment attachment = attachmentService.savePostImage(attachmentInfo);
                post.addAttachment(attachment);
            }
        }
        postService.updatePost(post, updateRequestDto.getTitle(), updateRequestDto.getContent());
        PostResponseDto postResponseDto = PostResponseDto.fromEntity(postService.getPost(post.getId()));

        return ResultResponse.of(ResultResponse.SUCCESS, postResponseDto);
    }

    @ApiOperation(value = "게시글 삭제", notes = "게시글번호로 게시글을 삭제한다.")
    @DeleteMapping("/{postId}")
    public ResultResponse<Void> deletePost(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시판번호", required = true) @PathVariable Long boardId,
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId) {
        boolean authorMatched = postService.isAuthorMatched(account, postId);
        if (!authorMatched) {
            throw new AuthorNotEqualException();
        }

        postService.deletePost(postId);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }
}
