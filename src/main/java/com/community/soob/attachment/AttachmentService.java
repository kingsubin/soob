package com.community.soob.attachment;

import com.community.soob.config.properties.SettingsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
@Transactional
@Service
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final SettingsProperties settingsProperties;

    private Attachment registerAttachment(AttachmentInfo attachmentInfo, String path) {
        String fileName = attachmentInfo.getOriginalFileName();
        String contentType = attachmentInfo.getContentType();

        // 확장자명이 없는 경우
        if (ObjectUtils.isEmpty(contentType)) {
            throw new AttachmentException("확장자명이 존재하지 않는 파일입니다.");
        }

        // jpeg, png 파일만 받아서 처리
        if (contentType.contains("image/jpeg")) {
            contentType = ".jpg";
        } else if (contentType.contains("image/png")) {
            contentType = ".png";
        } else {
            throw new AttachmentException("처리하지 않는 확장자명입니다.");
        }

        String hashedFileName = System.nanoTime() + contentType;
        String attachmentPath = settingsProperties.getAttachmentProperties().getPath();
        String savePath = attachmentPath + path;

        // 프로필이미지 저장폴더가 존재하지 않을 경우 폴더 생성
        if (!new File(savePath).exists()) {
            new File(savePath).mkdirs();
        }

        // 파일 저장 try-with-resource 사용
        String filePath = savePath + hashedFileName;
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(attachmentInfo.getContents());
        } catch (FileNotFoundException e) {
            throw new AttachmentException("파일을 생성할 수 없습니다.", e);
        } catch (IOException e) {
            throw new AttachmentException("파일을 저장하던 중 문제가 발생했습니다.", e);
        }

        // Attachment 저장
        AttachmentDto attachmentDto = AttachmentDto.builder()
                .fileName(fileName)
                .hashedFileName(hashedFileName)
                .filePath(filePath)
                .build();
        return attachmentRepository.save(attachmentDto.toEntity());
    }

    public Attachment saveProfileImage(AttachmentInfo attachmentInfo) {
        String profileImagePath = settingsProperties.getAttachmentProperties().getProfileImagePath();
        return registerAttachment(attachmentInfo, profileImagePath);
    }

    public Attachment savePostImage(AttachmentInfo attachmentInfo) {
        String postImagePath = settingsProperties.getAttachmentProperties().getPostImagePath();
        return registerAttachment(attachmentInfo, postImagePath);
    }

    public void deleteAttachment(long id) {
        attachmentRepository.deleteById(id);
    }
}
