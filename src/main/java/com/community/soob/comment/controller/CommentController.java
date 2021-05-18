package com.community.soob.comment.controller;

import com.community.soob.account.config.CurrentAccount;
import com.community.soob.account.domain.Account;
import com.community.soob.comment.controller.dto.CommentRequestDto;
import com.community.soob.comment.controller.dto.CommentResponseDto;
import com.community.soob.comment.domain.Comment;
import com.community.soob.comment.service.CommentService;
import com.community.soob.response.ResultResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@Api(tags = {"4, Comment"})
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@RestController
public class CommentController {
    private final CommentService commentService;

    @ApiOperation(value = "댓글 조회", notes = "특정 게시글의 댓글 전체 조회한다.")
    @GetMapping("/{postId}")
    public ResultResponse<Page<CommentResponseDto>> getCommentList(
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId,
            @ApiParam(value = "페이징") Pageable pageable) {
        Page<Comment> commentsByPostId = commentService.getComments(postId, pageable);
        Page<CommentResponseDto> pagingComments = commentsByPostId.map(CommentResponseDto::fromEntity);
        return ResultResponse.of(ResultResponse.SUCCESS, pagingComments);
    }

    @ApiOperation(value = "댓글 작성", notes = "특정 게시글에 댓글을 작성한다.")
    @PostMapping("/{postId}")
    public ResultResponse<Void> saveComment(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId,
            @ApiParam(value = "댓글작성DTO", required = true) @RequestBody @Valid final CommentRequestDto saveRequestDto) {
        commentService.saveComment(account, postId, saveRequestDto.getContent());
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "댓글 수정", notes = "특정 게시글의 댓글을 댓글번호로 수정한다.")
    @PutMapping("/{postId}/{commentId}")
    public ResultResponse<CommentResponseDto> updateComment(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId,
            @ApiParam(value = "댓글번호", required = true) @PathVariable Long commentId,
            @ApiParam(value = "댓글수정DTO", required = true) @RequestBody @Valid final CommentRequestDto requestDto) {
        commentService.updateComment(commentId, requestDto.getContent());
        CommentResponseDto commentResponseDto = CommentResponseDto.fromEntity(commentService.getComment(commentId));
        return ResultResponse.of(ResultResponse.SUCCESS, commentResponseDto);
    }

    @ApiOperation(value = "댓글 삭제", notes = "특정 게시글의 댓글을 댓글번호로 삭제한다.")
    @DeleteMapping("/{postId}/{commentId}")
    public ResultResponse<Void> deleteComment(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId,
            @ApiParam(value = "댓글번호", required = true) @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }
}
