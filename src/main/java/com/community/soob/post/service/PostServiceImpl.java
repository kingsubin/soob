package com.community.soob.post.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.attachment.Attachment;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final AccountRepository accountRepository;
    private final CommentService commentService;
    private final HeartService heartService;
    private final AttachmentService attachmentService;

    @Value("{attachment.url.post}")
    private String directoryName;

    @Transactional
    public Post getPost(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        post.increaseReadCount();
        postRepository.save(post);
        return post;
    }

    @Transactional
    @Override
    public void createPost(Account account, long boardId, String title, String content, @Nullable List<MultipartFile> files) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);
        Post post = Post.builder()
                .board(board)
                .author(account)
                .title(title)
                .content(content)
                .build();
        Post savedPost = postRepository.save(post);

        if (files != null && !files.isEmpty()) {
            if (files.size() == 1) {
                attachmentService.uploadPostImage(savedPost, files.get(0), directoryName);
            } else {
                attachmentService.uploadPostImages(savedPost, files, directoryName);
            }
        }

        account.increasePostPoint();
        account.updateLevel();
        accountRepository.save(account);
    }

    @Transactional
    @Override
    public void updatePost(Account account, long postId, String title, String content, @Nullable List<MultipartFile> files) {
        if (!isAuthorMatched(account, postId)) {
            throw new AuthorNotEqualException();
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        post.updatePost(title, content);
        Post savedPost = postRepository.save(post);

        if (files != null && !files.isEmpty()) {
            attachmentService.deletePostImages(post);
            if (files.size() == 1) {
                attachmentService.uploadPostImage(savedPost, files.get(0), directoryName);
            } else {
                attachmentService.uploadPostImages(savedPost, files, directoryName);
            }
        }
    }

    @Transactional
    @Override
    public void deletePost(Account account, long postId) {
        if (!isAuthorMatched(account, postId)) {
            throw new AuthorNotEqualException();
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        List<Attachment> attachments = post.getAttachments();
        if (!attachments.isEmpty()) {
            attachmentService.deletePostImages(post);
        }

        Long heartCount = heartService.getHeartCountForPost(postId);
        if (heartCount != null && heartCount != 0) {
            heartService.deleteAllHeartForPost(postId);
            account.decreasePostHeartPoint(heartCount);
        }

        Long commentCount = commentService.getCommentCountForPost(postId);
        if (commentCount != null && commentCount != 0) {
            commentService.deleteAllCommentForPost(postId);
        }

        postRepository.deleteById(postId);

        account.decreasePostPoint();
        account.updateLevel();
        accountRepository.save(account);
    }

    @Override
    public boolean isAuthorMatched(Account account, long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        return account.getId().equals(post.getAuthor().getId());
    }
}
