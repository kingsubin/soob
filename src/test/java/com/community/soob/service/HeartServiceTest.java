package com.community.soob.service;

import com.community.soob.comment.domain.CommentRepository;
import com.community.soob.heart.domain.HeartRepository;
import com.community.soob.heart.service.HeartService;
import com.community.soob.post.domain.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HeartServiceTest {
    @InjectMocks private HeartService heartService;
    @Mock private HeartRepository heartRepository;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;

    // ----- 게시글 하트 토글 -----
    @DisplayName("게시글 하트 토글 실패 - postId 존재하지않음")
    @Test
    void testToggleHeartToPostFailureByInvalidPostId() {

    }

    @DisplayName("게시글 하트 토글 성공 - 안누른 상태일때 누르기")
    @Test
    void testToggleHeartToPostSuccessWhenToggleOff() {

    }

    @DisplayName("게시글 하트 토글 성공 - 누른 상태일때 누르기")
    @Test
    void testToggleHeartToPostSuccessWhenToggleOn() {

    }

    // ----- 댓글 하트 토글 -----
    @DisplayName("게시글 하트 토글 실패 - postId 존재하지않음")
    @Test
    void testToggleHeartToCommentFailureByInvalidPostId() {

    }

    @DisplayName("게시글 하트 토글 성공 - 안누른 상태일때 누르기")
    @Test
    void testToggleHeartToCommentSuccessWhenToggleOff() {

    }

    @DisplayName("게시글 하트 토글 성공 - 누른 상태일때 누르기")
    @Test
    void testToggleHeartToCommentSuccessWhenToggleOn() {

    }
}
