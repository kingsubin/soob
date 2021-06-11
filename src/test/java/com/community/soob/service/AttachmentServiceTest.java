package com.community.soob.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.attachment.Attachment;
import com.community.soob.attachment.AttachmentRepository;
import com.community.soob.attachment.AttachmentService;
import com.community.soob.attachment.S3Service;
import com.community.soob.post.domain.Board;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {
    @InjectMocks private AttachmentService attachmentService;
    @Mock private AttachmentRepository attachmentRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PostRepository postRepository;
    @Mock private S3Service s3Service;

    private final String profileDirectoryName = "profile/";
    private final String postDirectoryName = "post/";

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

    @DisplayName("프로필이미지 업로드 성공")
    @Test
    void testProfileImageUploadSuccess() throws IOException {
        // given
        Account account = createAccount();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                "image/jpeg",
                new ClassPathResource("/images/profileImage.jpg").getInputStream()
        );
        Attachment savedAttachment = Attachment.builder()
                .id(1L)
                .fileName("name")
                .filePath("path")
                .build();

        given(attachmentRepository.save(any()))
                .willReturn(savedAttachment);

        // when
        attachmentService.uploadProfileImage(account, multipartFile, profileDirectoryName);

        // then
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());
        assertEquals(
                savedAttachment,
                accountArgumentCaptor.getValue().getProfileImage()
        );
    }

    @DisplayName("게시글이미지 업로드 성공")
    @Test
    void testPostImageUploadSuccess() throws IOException {
        // given
        Post post = createPost();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                "image/jpeg",
                new ClassPathResource("/images/profileImage.jpg").getInputStream()
        );
        Attachment savedAttachment = Attachment.builder()
                .id(1L)
                .fileName("name")
                .filePath("path")
                .build();

        given(attachmentRepository.save(any()))
                .willReturn(savedAttachment);

        // when
        attachmentService.uploadPostImage(post, multipartFile, postDirectoryName);
        
        // then
        ArgumentCaptor<Attachment> attachmentArgumentCaptor = ArgumentCaptor.forClass(Attachment.class);
        then(attachmentRepository).should(times(2)).save(attachmentArgumentCaptor.capture());

        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(postArgumentCaptor.capture());

        assertEquals(
                post,
                attachmentArgumentCaptor.getValue().getPost()
        );
        assertEquals(
                savedAttachment,
                postArgumentCaptor.getValue().getAttachments().get(0)
        );
    }

    @DisplayName("프로필이미지 삭제 성공")
    @Test
    void testProfileImageDeleteSuccess() {
        // given
        Attachment attachment = Attachment.builder()
                .id(1L)
                .fileName("fileName")
                .filePath("filePath")
                .build();
        Account account = Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.NOT_PERMITTED)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(attachment)
                .build();

        // when
        attachmentService.deleteProfileImage(account);

        // then
        then(attachmentRepository).should().delete(attachment);
        then(s3Service).should().delete(attachment.getFileName());
    }

    @DisplayName("게시글이미지 삭제 성공")
    @Test
    void testPostImagesDeleteSuccess() {
        // given
        List<Attachment> attachments = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            attachments.add(
                    Attachment.builder()
                            .id((long) i)
                            .fileName(String.valueOf(i))
                            .filePath(String.valueOf(i))
                            .build()
            );
        }
        Post post = Post.builder()
                .id(1L)
                .board(createBoard())
                .author(createAccount())
                .title("title1")
                .content("content1")
                .attachments(attachments)
                .readCount(0)
                .heartCount(0)
                .build();

        // when
        attachmentService.deletePostImages(post);
        
        // then
        then(s3Service).should(times(3)).delete(any());
        then(attachmentRepository).should(times(3)).delete(any());

        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(postArgumentCaptor.capture());

        assertEquals(0, postArgumentCaptor.getValue().getAttachments().size());
    }
}

