package com.community.soob.service;

import com.community.soob.comment.domain.CommentRepository;
import com.community.soob.comment.service.CommentService;
import com.community.soob.heart.service.HeartService;
import com.community.soob.post.domain.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @InjectMocks private CommentService commentService;
    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private HeartService heartService;

    // ----- 댓글 작성 -----
    @DisplayName("댓글 작성 실패 - postId 존재하지 않음")
    @Test
    void testCreateCommentFailureByInvalidPostId() {

    }

    @DisplayName("댓글 작성 성공")
    @Test
    void testCreateCommentSuccess() {

    }

    // ----- 댓글 수정 -----
    @DisplayName("댓글 수정 실패 - commentId 존재하지 않음")
    @Test
    void testUpdateCommentFailureByInvalidCommentId() {

    }

    @DisplayName("댓글 수정 실패 - 작성자가 아님")
    @Test
    void testUpdateCommentFailureByAuthorNotMatched() {

    }

    @DisplayName("댓글 수정 성공 - 댓글 내용")
    @Test
    void testUpdateCommentSuccess() {

    }

    // ----- 댓글 삭제 -----
    @DisplayName("댓글 삭제 실패 - 작성자가 아님")
    @Test
    void testDeleteCommentFailureByAuthorNotMatched() {

    }

    @DisplayName("댓글 삭제 성공 - 하트가 존재하는 댓글")
    @Test
    void testDeleteCommentSuccessWithHeart() {

    }

    @DisplayName("댓글 삭제 실패 - 하트가 존재하지 않은 댓글")
    @Test
    void testDeleteCommentSuccessWithoutHeart() {

    }
}
