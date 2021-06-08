package com.community.soob.service;

import com.community.soob.attachment.AttachmentService;
import com.community.soob.comment.service.CommentService;
import com.community.soob.heart.service.HeartService;
import com.community.soob.post.domain.BoardRepository;
import com.community.soob.post.domain.PostRepository;
import com.community.soob.post.service.PostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {
    @InjectMocks private PostServiceImpl postServiceImpl;
    @Mock private PostRepository postRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private CommentService commentService;
    @Mock private HeartService heartService;
    @Mock private AttachmentService attachmentService;

    private final String directoryName = "post/";

    // ----- 게시글 조회 -----
    @DisplayName("게시글 조회 실패 - postId 존재하지 않음")
    @Test
    void testGetPostFailureByInvalidPostId() {
    }

    @DisplayName("게시글 조회 성공 - 조회수 증가")
    @Test
    void testGetPostSuccess() {
    }

    // ----- 게시글 작성 -----
    @DisplayName("게시글 작성 실패 - boardId 존재하지않음")
    @Test
    void testCreatePostFailureByInvalidBoardId() {
    }

    @DisplayName("게시글 작성 성공 - 이미지 없음")
    @Test
    void testCreatePostSuccessWithoutImage() {
    }

    @DisplayName("게시글 작성 성공 - 1개의 이미지")
    @Test
    void testCreatePostSuccessWithImage() {
    }

    @DisplayName("게시글 작성 성공 - 복수의 이미지")
    @Test
    void testCreatePostSuccessWithImages() {
    }

    // ----- 게시글 수정 -----
    @DisplayName("게시글 수정 성공 - 이미지 없음")
    @Test
    void testUpdatePostSuccessWithoutImage() {
    }

    @DisplayName("게시글 수정 성공 - 이미지 존재")
    @Test
    void testUpdatePostSuccessWithImages() {
    }

    // ----- 게시글 삭제 -----
    @DisplayName("게시글 삭제 실패 - postId 존재하지 않음")
    @Test
    void testDeletePostFailureByInvalidPostId() {
    }

    @DisplayName("게시글 삭제 성공 - 추천, 댓글, 이미지 존재시 삭제")
    @Test
    void testDeletePostSuccessWithEtc() {
    }

    @DisplayName("게시글 삭제 성공 - 추천, 댓글, 이미지 없을때 삭제")
    @Test
    void testDeletePostSuccessWithoutEtc() {
    }

}
