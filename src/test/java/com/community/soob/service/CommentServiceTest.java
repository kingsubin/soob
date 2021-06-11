package com.community.soob.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.Role;
import com.community.soob.comment.domain.Comment;
import com.community.soob.comment.domain.CommentRepository;
import com.community.soob.comment.exception.CommentNotFoundException;
import com.community.soob.comment.service.CommentService;
import com.community.soob.heart.service.HeartService;
import com.community.soob.post.domain.Board;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import com.community.soob.post.exception.PostNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @InjectMocks private CommentService commentService;
    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private HeartService heartService;

    private Account createAccount() {
        return Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.NOT_PERMITTED)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();
    }

    private Board createBoard() {
        return Board.builder()
                .id(1L)
                .name("QNA")
                .build();
    }

    private Post createPost() {
        return Post.builder()
                .id(1L)
                .board(createBoard())
                .author(createAccount())
                .title("title1")
                .content("content1")
                .attachments(new ArrayList<>())
                .readCount(0)
                .heartCount(0)
                .build();
    }

    // ----- 댓글 작성 -----
    @DisplayName("댓글 작성 실패 - postId 존재하지 않음")
    @Test
    void testCreateCommentFailureByInvalidPostId() {
        // given
        long postId = 10L;
        String content = "test-comment";
        Account account = createAccount();

        // when
        // then
        assertThrows(PostNotFoundException.class,
                () -> commentService.createComment(account, postId, content)
        );
    }

    @DisplayName("댓글 작성 성공")
    @Test
    void testCreateCommentSuccess() {
        // given
        long postId = 1L;
        String content = "test-comment";
        Account account = createAccount();
        Post post = createPost();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        commentService.createComment(account, postId, content);
        
        // then
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        then(commentRepository).should().save(commentArgumentCaptor.capture());

        assertEquals(post, commentArgumentCaptor.getValue().getPost());
        assertEquals(account, commentArgumentCaptor.getValue().getAuthor());
        assertEquals(content, commentArgumentCaptor.getValue().getContent());
    }

    // ----- 댓글 수정 -----
    @DisplayName("댓글 수정 실패 - commentId 존재하지 않음")
    @Test
    void testUpdateCommentFailureByInvalidCommentId() {
        // given
        long commentId = 10L;
        String content = "modify-content";
        Account account = createAccount();

        // when
        // then
        assertThrows(CommentNotFoundException.class,
                () -> commentService.updateComment(account, commentId, content)
        );
    }

    @DisplayName("댓글 수정 성공")
    @Test
    void testUpdateCommentSuccess() {
        // given
        long commentId = 1L;
        String content = "modify-content";
        Account account = createAccount();
        Comment comment = Comment.builder()
                .id(1L)
                .author(account)
                .post(createPost())
                .content("comment")
                .heartCount(0)
                .build();
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));

        // when
        commentService.updateComment(account, commentId, content);

        // then
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        then(commentRepository).should().save(commentArgumentCaptor.capture());

        assertEquals(content, commentArgumentCaptor.getValue().getContent());
    }

    // ----- 댓글 삭제 -----
    @DisplayName("댓글 삭제 성공 - 하트가 존재하는 댓글")
    @Test
    void testDeleteCommentSuccessWithHeart() {
        // given
        long commentId = 1L;
        Account account = createAccount();
        Comment comment = Comment.builder()
                .id(1L)
                .author(account)
                .post(createPost())
                .content("comment")
                .heartCount(0)
                .build();

        given(heartService.getHeartCountForComment(commentId)).willReturn(10L);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(account, commentId);

        // then
        then(heartService).should().deleteAllHeartForComment(commentId);
        then(commentRepository).should().deleteById(commentId);
    }

    @DisplayName("댓글 삭제 성공 - 하트가 존재하지 않은 댓글")
    @Test
    void testDeleteCommentSuccessWithoutHeart() {
        // given
        long commentId = 1L;
        Account account = createAccount();
        Comment comment = Comment.builder()
                .id(1L)
                .author(account)
                .post(createPost())
                .content("comment")
                .heartCount(0)
                .build();

        given(heartService.getHeartCountForComment(commentId)).willReturn(null);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(account, commentId);

        // then
        then(heartService).should(never()).deleteAllHeartForComment(commentId);
        then(commentRepository).should().deleteById(commentId);
    }

    // ----- 댓글 작성자 확인 -----
    @DisplayName("댓글 작성자 확인 실패 - commentId 존재하지 않음")
    @Test
    void testIsAuthorMatchedFailureByInvalidCommentId() {
        // given
        Account account = createAccount();
        long commentId = 10L;

        // when
        // then
        assertThrows(CommentNotFoundException.class,
                () -> commentService.isAuthorMatched(account, commentId)
        );
    }

    @DisplayName("댓글 작성자 확인 실패 - 작성자가 아님")
    @Test
    void testIsAuthorMatchedFailureByAuthorNotEqual() {
        // given
        long commentId = 1L;
        Account accountA = createAccount();
        Account accountB = Account.builder()
                .id(3L)
                .email("test3@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt23")
                .nickname("test3")
                .role(Role.NOT_PERMITTED)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO3")
                .profileImage(null)
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .author(accountB)
                .post(createPost())
                .content("comment")
                .heartCount(0)
                .build();
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));

        // when
        // then
        assertFalse(commentService.isAuthorMatched(accountA, commentId));
    }

    @DisplayName("댓글 작성자 확인 성공")
    @Test
    void testIsAuthorMatchedSuccess() {
        // given
        long commentId = 1L;
        Account account = createAccount();
        Comment comment = Comment.builder()
                .id(1L)
                .author(account)
                .post(createPost())
                .content("comment")
                .heartCount(0)
                .build();
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));

        // when
        // then
        assertTrue(commentService.isAuthorMatched(account, commentId));
    }
}
