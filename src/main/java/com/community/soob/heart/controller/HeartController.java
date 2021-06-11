package com.community.soob.heart.controller;

import com.community.soob.account.config.CurrentAccount;
import com.community.soob.account.domain.Account;
import com.community.soob.heart.service.HeartService;
import com.community.soob.response.ResultResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = {"5, Heart"})
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearts")
@RestController
public class HeartController {
    private final HeartService heartService;

    @ApiOperation(value = "게시글 하트", notes = "특정 게시글 하트 클릭")
    @PostMapping("/posts/{postId}")
    public ResultResponse<Void> putHeartToPost(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId) {
        heartService.toggleHeartToPost(account, postId);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "댓글 하트", notes = "특정 댓글 하트 클릭")
    @PostMapping("/comments/{commentId}")
    public ResultResponse<Void> putHeartToComment(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "댓글번호", required = true) @PathVariable Long commentId) {
        heartService.toggleHeartToComment(account, commentId);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "게시글 하트 갯수 조회", notes = "특정 게시글 하트 갯수 조회")
    @GetMapping("/posts/{postId}")
    public ResultResponse<Long> getHeartCountForPost(
            @ApiParam(value = "게시글번호", required = true) @PathVariable Long postId) {
        return ResultResponse.of(ResultResponse.SUCCESS, heartService.getHeartCountForPost(postId));
    }

    @ApiOperation(value = "댓글 하트 갯수 조회", notes = "특정 댓글 하트 갯수 조회")
    @GetMapping("/comments/{commentId}")
    public ResultResponse<Long> getHeartCountForComment(
            @ApiParam(value = "댓글번호", required = true) @PathVariable Long commentId) {
        return ResultResponse.of(ResultResponse.SUCCESS, heartService.getHeartCountForComment(commentId));
    }
}
