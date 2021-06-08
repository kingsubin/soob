package com.community.soob.service;

import com.community.soob.account.domain.AccountRepository;
import com.community.soob.attachment.*;
import com.community.soob.post.domain.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {
    @InjectMocks private AttachmentService attachmentService;
    @Mock private AttachmentRepository attachmentRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PostRepository postRepository;
    @Mock private S3Service s3Service;

    private final String directoryName = "profile/";

    // 프로필이미지 업로드
    @DisplayName("프로필이미지 업로드 성공")
    @Test
    void testProfileImageUploadSuccess() {

    }

    // 게시글이미지 업로드
    @DisplayName("게시글이미지 업로드 성공")
    @Test
    void testPostImageUploadSuccess() {

    }

    // 게시글이미지 복수 업로드
    @DisplayName("게시글이미지 복수 업로드 성공")
    @Test
    void testPostImagesUploadSuccess() {

    }

    // 게시글이미지 삭제
    @DisplayName("게시글이미지 삭제 성공")
    @Test
    void testPostImagesDeleteSuccess() {

    }

    // 프로필이미지 삭제
    @DisplayName("프로필이미지 삭제 성공")
    @Test
    void testProfileImageDeleteSuccess() {

    }
}

