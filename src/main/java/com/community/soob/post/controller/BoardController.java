package com.community.soob.post.controller;

import com.community.soob.post.controller.dto.PostResponseDto;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import com.community.soob.post.service.BoardService;
import com.community.soob.response.ResultResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = {"2, Board"})
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
@Controller
public class BoardController {
    private final PostRepository postRepository;
    private final BoardService boardService;

    @ApiOperation(value = "전체 게시판 전체 게시글 조회", notes = "전체 게시판의 게시글 전체를 조회한다.")
    @GetMapping
    public ResultResponse<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> posts = toPostResponseDtos(postRepository.findAll());
        return ResultResponse.of(ResultResponse.SUCCESS, posts);
    }

    @ApiOperation(value = "일정 게시판 전체 게시글 조회", notes = "게시판 번호로 게시글 전체를 조회한다.")
    @GetMapping("/{boardId}")
    public ResultResponse<Page<PostResponseDto>> getPostsOfBoard(
            @ApiParam(value = "게시판번호", required = true) @PathVariable Long boardId,
            @ApiParam(value = "페이징") Pageable pageable) {
        Page<Post> postsByBoardId = boardService.getPostsByBoardId(boardId, pageable);
        Page<PostResponseDto> pagingPosts = postsByBoardId.map(PostResponseDto::fromEntity);
        return ResultResponse.of(ResultResponse.SUCCESS, pagingPosts);
    }

    public static List<PostResponseDto> toPostResponseDtos(List<Post> posts) {
        return posts.stream()
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
