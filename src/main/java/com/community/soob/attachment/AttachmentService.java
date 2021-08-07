package com.community.soob.attachment;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Transactional
@Service
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final String profileImageDirectory;
    private final String postImageDirectory;

    public AttachmentService(AttachmentRepository attachmentRepository, AccountRepository accountRepository, PostRepository postRepository, S3Service s3Service, @Value("{attachment.url.profile}") String profileImageDirectory, @Value("{attachment.url.post}") String postImageDirectory) {
        this.attachmentRepository = attachmentRepository;
        this.accountRepository = accountRepository;
        this.postRepository = postRepository;
        this.s3Service = s3Service;
        this.profileImageDirectory = profileImageDirectory;
        this.postImageDirectory = postImageDirectory;
    }

    public void uploadProfileImage(Account account, MultipartFile file) {
        String fileName = profileImageDirectory + createFileName(file.getOriginalFilename());
        String path = uploadImage(file, fileName);
        Attachment attachment = Attachment.builder()
                .fileName(fileName)
                .filePath(path)
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);

        account.updateProfileImage(savedAttachment);
        accountRepository.save(account);
    }

    public void uploadPostImage(Post post, MultipartFile file) {
        String fileName = postImageDirectory + createFileName(file.getOriginalFilename());
        String path = uploadImage(file, fileName);
        Attachment attachment = Attachment.builder()
                .fileName(fileName)
                .filePath(path)
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);

        savedAttachment.setPost(post);
        attachmentRepository.save(savedAttachment);

        post.addAttachment(savedAttachment);
        postRepository.save(post);
    }

    public void uploadPostImages(Post post, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            uploadPostImage(post, file);
        }
    }

    public void deletePostImages(Post post) {
        List<Attachment> attachments = post.getAttachments();
        for (Attachment attachment : attachments) {
            s3Service.delete(attachment.getFileName());
            attachmentRepository.delete(attachment);
        }

        post.removeAttachments();
        postRepository.save(post);
    }

    public void deleteProfileImage(Account account) {
        Attachment profileImage = account.getProfileImage();
        attachmentRepository.delete(profileImage);
        s3Service.delete(profileImage.getFileName());
    }

    public String uploadImage(MultipartFile file, String fileName) {
        try {
            s3Service.upload(file, fileName);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("파일 변환 중 에러가 발생하였습니다 (%s)", file.getOriginalFilename()));
        }
        return s3Service.getFileUrl(fileName);
    }

    private String createFileName(String originalFileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(originalFileName));
    }

    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format("잘못된 형식의 파일 (%s) 입니다", fileName));
        }
    }
}
