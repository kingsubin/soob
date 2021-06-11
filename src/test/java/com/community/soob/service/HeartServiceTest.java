package com.community.soob.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.Role;
import com.community.soob.comment.domain.Comment;
import com.community.soob.comment.domain.CommentRepository;
import com.community.soob.comment.exception.CommentNotFoundException;
import com.community.soob.heart.domain.Heart;
import com.community.soob.heart.domain.HeartRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class HeartServiceTest {
    @InjectMocks private HeartService heartService;
    @Mock private HeartRepository heartRepository;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;

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

    // ----- 게시글 하트 토글 -----
    @DisplayName("게시글 하트 토글 실패 - postId 존재하지 않음")
    @Test
    void testToggleHeartToPostFailureByInvalidPostId() {
        // given
        long postId = 10L;
        Account account = createAccount();

        // when
        // then
        assertThrows(PostNotFoundException.class,
                () -> heartService.toggleHeartToPost(account, postId)
        );
    }

    @DisplayName("게시글 하트 토글 성공 - 안누른 상태일때 누르기")
    @Test
    void testToggleHeartToPostSuccessWhenToggleOff() {
        // given
        long postId = 1L;
        Account account = createAccount();
        Post post = createPost();

        given(postRepository.findById(postId))
                .willReturn(Optional.of(post));
        given(heartRepository.findByPostIdAndAccountId(postId, account.getId()))
                .willReturn(null);

        // when
        heartService.toggleHeartToPost(account, postId);

        // then
        then(heartRepository).should().save(any());

        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(postArgumentCaptor.capture());

        assertEquals(1, postArgumentCaptor.getValue().getHeartCount());
    }

    @DisplayName("게시글 하트 토글 성공 - 누른 상태일때 누르기")
    @Test
    void testToggleHeartToPostSuccessWhenToggleOn() {
        // given
        long postId = 1L;
        Account account = createAccount();
        Post post = createPost();
        Heart foundHeart = Heart.builder()
                .id(1L)
                .account(account)
                .post(post)
                .build();

        given(postRepository.findById(postId))
                .willReturn(Optional.of(post));
        given(heartRepository.findByPostIdAndAccountId(postId, account.getId()))
                .willReturn(foundHeart);

        // when
        heartService.toggleHeartToPost(account, postId);

        // then
        then(heartRepository).should().delete(eq(foundHeart));

        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(postArgumentCaptor.capture());

        assertEquals(-1, postArgumentCaptor.getValue().getHeartCount());
    }

    // ----- 댓글 하트 토글 -----
    @DisplayName("댓글 하트 토글 실패 - postId 존재하지 않음")
    @Test
    void testToggleHeartToCommentFailureByInvalidPostId() {
        // given
        long commentId = 10L;
        Account account = createAccount();

        // when
        // then
        assertThrows(CommentNotFoundException.class,
                () -> heartService.toggleHeartToComment(account, commentId)
        );
    }

    @DisplayName("댓글 하트 토글 성공 - 안누른 상태일때 누르기")
    @Test
    void testToggleHeartToCommentSuccessWhenToggleOff() {
        // given
        long commentId = 1L;
        Account account = createAccount();
        Post post = createPost();
        Comment comment = Comment.builder()
                .id(1L)
                .author(account)
                .post(post)
                .content("content")
                .heartCount(0)
                .build();

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(heartRepository.findByCommentIdAndAccountId(commentId, account.getId()))
                .willReturn(null);

        // when
        heartService.toggleHeartToComment(account, commentId);

        // then
        then(heartRepository).should().save(any());

        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        then(commentRepository).should().save(commentArgumentCaptor.capture());

        assertEquals(1, commentArgumentCaptor.getValue().getHeartCount());
    }

    @DisplayName("댓글 하트 토글 성공 - 누른 상태일때 누르기")
    @Test
    void testToggleHeartToCommentSuccessWhenToggleOn() {
        // given
        long commentId = 1L;
        Account account = createAccount();
        Post post = createPost();
        Comment comment = Comment.builder()
                .id(1L)
                .author(account)
                .post(post)
                .content("content")
                .heartCount(0)
                .build();
        Heart foundHeart = Heart.builder()
                .id(1L)
                .account(account)
                .comment(comment)
                .build();

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(heartRepository.findByCommentIdAndAccountId(commentId, account.getId()))
                .willReturn(foundHeart);

        // when
        heartService.toggleHeartToComment(account, commentId);

        // then
        then(heartRepository).should().delete(any());

        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        then(commentRepository).should().save(commentArgumentCaptor.capture());

        assertEquals(-1, commentArgumentCaptor.getValue().getHeartCount());
    }
}
