package com.community.soob.post.controller;

import com.community.soob.account.config.CurrentAccount;
import com.community.soob.account.domain.Account;
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
import java.util.List;

@Api(tags = {"3, Post"})
@RequestMapping("/api/v1/boards/{boardId}/posts")
@RequiredArgsConstructor
@Controller
public class PostController {
    private final PostService postService;

    @ApiOperation(value = "게시글 작성", notes = "회원이 게시글을 작성한다.")
    @PostMapping
    public ResultResponse<Void> savePost(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시판 번호", required = true) @PathVariable Long boardId,
            @ApiParam(value = "게시글작성DTO", required = true) @Valid @RequestBody final PostRequestDto saveRequestDto,
            @ApiParam(value = "게시글이미지") @RequestPart(name = "files") List<MultipartFile> files) {
        postService.createPost(account, boardId, saveRequestDto.getTitle(), saveRequestDto.getContent(), files);
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
        boolean authorMatched = postService.isAuthorMatched(account, postId);
        if (!authorMatched) {
            throw new AuthorNotEqualException();
        }
        Post post = postService.getPost(postId);
        postService.updatePost(post, updateRequestDto.getTitle(), updateRequestDto.getContent(), files);
        return ResultResponse.of(ResultResponse.SUCCESS, PostResponseDto.fromEntity(post));
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
