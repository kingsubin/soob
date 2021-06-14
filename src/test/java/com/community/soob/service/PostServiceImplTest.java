package com.community.soob.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.attachment.AttachmentService;
import com.community.soob.comment.service.CommentService;
import com.community.soob.heart.service.HeartService;
import com.community.soob.post.domain.Board;
import com.community.soob.post.domain.BoardRepository;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import com.community.soob.post.exception.AuthorNotEqualException;
import com.community.soob.post.exception.BoardNotFoundException;
import com.community.soob.post.exception.PostNotFoundException;
import com.community.soob.post.service.PostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {
    @InjectMocks private PostServiceImpl postServiceImpl;
    @Mock private PostRepository postRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CommentService commentService;
    @Mock private HeartService heartService;
    @Mock private AttachmentService attachmentService;

    private Account createAccount() {
        return Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.LEVEL_1)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .levelPoint(50)
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

    // ----- 게시글 조회 -----
    @DisplayName("게시글 조회 실패 - postId 존재하지 않음")
    @Test
    void testGetPostFailureByInvalidPostId() {
        // given
        long postId = 1L;

        // when
        // then
        assertThrows(PostNotFoundException.class, () -> {
            postServiceImpl.getPost(postId);
        });
    }

    @DisplayName("게시글 조회 성공 - 조회수 증가")
    @Test
    void testGetPostSuccess() {
        // given
        long postId = 1L;
        Post post = createPost();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        postServiceImpl.getPost(postId);

        // then
        assertEquals(1, post.getReadCount());
        then(postRepository).should().save(post);
    }

    // ----- 게시글 작성 -----
    @DisplayName("게시글 작성 실패 - boardId 존재하지않음")
    @Test
    void testCreatePostFailureByInvalidBoardId() {
        // given
        Account account = createAccount();
        long boardId = 1L;
        String title = "test-title";
        String content = "test-content";

        // when
        // then
        assertThrows(BoardNotFoundException.class, () -> {
            postServiceImpl.createPost(account, boardId, title, content, null);
        });
    }

    @DisplayName("게시글 작성 성공 - 이미지 없음")
    @Test
    void testCreatePostSuccessWithoutImage() {
        // given
        Account account = createAccount();
        long boardId = 1L;
        String title = "test-title";
        String content = "test-content";
        Board board = createBoard();
        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        postServiceImpl.createPost(account, boardId, title, content, null);

        // then
        then(postRepository).should().save(any());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());

        assertEquals(60, accountArgumentCaptor.getValue().getLevelPoint());
    }

    @DisplayName("게시글 작성 성공 - 1개의 이미지")
    @Test
    void testCreatePostSuccessWithImage() throws IOException {
        // given
        long boardId = 1L;
        String title = "test-title";
        String content = "test-content";
        Account account = createAccount();
        Board board = createBoard();
        List<MultipartFile> files = new ArrayList<>();
        files.add(new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                "image/jpeg",
                new ClassPathResource("/images/profileImage.jpg").getInputStream()
        ));
        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        postServiceImpl.createPost(account, boardId, title, content, files);

        // then
        then(postRepository).should().save(any());
        then(attachmentService).should().uploadPostImage(any(), eq(files.get(0)), any());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());

        assertEquals(60, accountArgumentCaptor.getValue().getLevelPoint());
    }

    @DisplayName("게시글 작성 성공 - 복수의 이미지")
    @Test
    void testCreatePostSuccessWithImages() throws IOException {
        // given
        long boardId = 1L;
        String title = "test-title";
        String content = "test-content";
        Account account = createAccount();
        Board board = createBoard();
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            files.add(
                    new MockMultipartFile(
                            String.valueOf(i),
                            String.valueOf(i),
                            "image/jpeg",
                            new ClassPathResource("/images/profileImage.jpg").getInputStream()
                    )
            );
        }
        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        postServiceImpl.createPost(account, boardId, title, content, files);

        // then
        then(postRepository).should().save(any());
        then(attachmentService).should().uploadPostImages(any(), eq(files), any());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());

        assertEquals(60, accountArgumentCaptor.getValue().getLevelPoint());
    }

    @DisplayName("게시글 작성 성공 - 게시글 작성을 통해 작성자의 레벨이 2로 업데이트")
    @Test
    void testCreatePostAndAccountUpdateLevelSuccess() {
        // given
        Account account = Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.NOT_PERMITTED)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .levelPoint(240)
                .profileImage(null)
                .build();
        long boardId = 1L;
        String title = "test-title";
        String content = "test-content";
        Board board = createBoard();
        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        postServiceImpl.createPost(account, boardId, title, content, null);

        // then
        then(postRepository).should().save(any());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());

        assertEquals(250, accountArgumentCaptor.getValue().getLevelPoint());
        assertEquals(Role.LEVEL_2, accountArgumentCaptor.getValue().getRole());
    }

    // ----- 게시글 수정 -----
    @DisplayName("게시글 수정 실패 -  작성자 불일치")
    @Test
    void testUpdatePostFailureByAuthorNotEqual() {
        // given
        Account account = Account.builder()
                .id(3L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.NOT_PERMITTED)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();
        Post post = Post.builder()
                .id(1L)
                .board(createBoard())
                .author(createAccount())
                .title("title1")
                .content("content1")
                .attachments(new ArrayList<>())
                .readCount(0)
                .heartCount(0)
                .build();
        long postId = 1L;
        String title = "modify-title";
        String content = "modify-content";

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        // then
        assertThrows(AuthorNotEqualException.class, () -> {
            postServiceImpl.updatePost(account, postId, title, content, null);
        });
    }

    @DisplayName("게시글 수정 성공 - 이미지 없음")
    @Test
    void testUpdatePostSuccessWithoutImage() {
        // given
        Account account = createAccount();
        Post post = createPost();
        long postId = 1L;
        String title = "modify-title";
        String content = "modify-content";

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        postServiceImpl.updatePost(account, postId, title, content, null);

        // then
        assertEquals(title, post.getTitle());
        assertEquals(content, post.getContent());
        then(postRepository).should().save(any());
    }

    @DisplayName("게시글 수정 성공 - 이미지 존재")
    @Test
    void testUpdatePostSuccessWithImages() throws IOException {
        // given
        Account account = createAccount();
        Post post = createPost();
        long postId = 1L;
        String title = "modify-title";
        String content = "modify-content";
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            files.add(
                    new MockMultipartFile(
                            String.valueOf(i),
                            String.valueOf(i),
                            "image/jpeg",
                            new ClassPathResource("/images/profileImage.jpg").getInputStream()
                    )
            );
        }
        Post savedPost = Post.builder()
                .id(1L)
                .board(post.getBoard())
                .author(post.getAuthor())
                .title(title)
                .content(content)
                .attachments(new ArrayList<>())
                .readCount(0)
                .heartCount(0)
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postRepository.save(post)).willReturn(savedPost);

        // when
        postServiceImpl.updatePost(account, postId, title, content, files);

        // then
        assertEquals(title, post.getTitle());
        assertEquals(content, post.getContent());
        then(postRepository).should().save(any());
        then(attachmentService).should().deletePostImages(post);
        then(attachmentService).should().uploadPostImages(any(), any(), any());
    }

    // ----- 게시글 삭제 -----
    @DisplayName("게시글 삭제 성공 - 추천, 댓글 존재시 삭제")
    @Test
    void testDeletePostSuccessWithEtc() throws IOException {
        // given
        long postId = 1L;
        Account account = createAccount();
        Post post = createPost();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(heartService.getHeartCountForPost(postId)).willReturn(10L);
        given(commentService.getCommentCountForPost(postId)).willReturn(10L);

        // when
        postServiceImpl.deletePost(account, postId);

        // then
        then(heartService).should().deleteAllHeartForPost(postId);
        then(commentService).should().deleteAllCommentForPost(postId);
        then(postRepository).should().deleteById(postId);

        // 50 - 210 => expected -160
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());
        assertEquals(-160, accountArgumentCaptor.getValue().getLevelPoint());
        assertEquals(Role.LEVEL_1, accountArgumentCaptor.getValue().getRole());
    }

    @DisplayName("게시글 삭제 성공 - 추천, 댓글 없을때 삭제")
    @Test
    void testDeletePostSuccessWithoutEtc() {
        // given
        long postId = 1L;
        Account account = createAccount();
        Post post = createPost();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(heartService.getHeartCountForPost(postId)).willReturn(0L);
        given(commentService.getCommentCountForPost(postId)).willReturn(0L);

        // when
        postServiceImpl.deletePost(account, postId);

        // then
        then(heartService).should(never()).deleteAllHeartForPost(postId);
        then(commentService).should(never()).deleteAllCommentForPost(postId);
        then(postRepository).should().deleteById(postId);

        // 50 - 10 => expected 40
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());
        assertEquals(40, accountArgumentCaptor.getValue().getLevelPoint());
        assertEquals(Role.LEVEL_1, accountArgumentCaptor.getValue().getRole());
    }

    @DisplayName("게시글 삭제 성공 - 게시글 삭제로 인해 레벨 1로 다운")
    @Test
    void testDeletePostAndAccountUpdateLevelSuccess() {
        // given
        long postId = 1L;
        Account account = Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.LEVEL_2)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .levelPoint(250)
                .profileImage(null)
                .build();
        Post post = createPost();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(heartService.getHeartCountForPost(postId)).willReturn(0L);
        given(commentService.getCommentCountForPost(postId)).willReturn(0L);

        // when
        postServiceImpl.deletePost(account, postId);

        // then
        then(heartService).should(never()).deleteAllHeartForPost(postId);
        then(commentService).should(never()).deleteAllCommentForPost(postId);
        then(postRepository).should().deleteById(postId);

        // 250 - 10 => expected 240, level 1
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());
        assertEquals(240, accountArgumentCaptor.getValue().getLevelPoint());
        assertEquals(Role.LEVEL_1, accountArgumentCaptor.getValue().getRole());
    }
}
