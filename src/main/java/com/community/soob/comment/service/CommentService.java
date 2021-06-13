package com.community.soob.comment.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.comment.domain.Comment;
import com.community.soob.comment.domain.CommentRepository;
import com.community.soob.comment.exception.CommentNotFoundException;
import com.community.soob.heart.service.HeartService;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import com.community.soob.post.exception.AuthorNotEqualException;
import com.community.soob.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final HeartService heartService;

    @Transactional
    public void createComment(Account account, long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        Comment comment = Comment.builder()
                .post(post)
                .author(account)
                .content(content)
                .build();

        commentRepository.save(comment);

        account.increaseCommentPoint();
        account.updateLevel();
        accountRepository.save(account);
    }

    public Page<Comment> getComments(long postId, Pageable pageable) {
        pageable = PageRequest.of((pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1), 10);
        return commentRepository.findAllByPostId(postId, pageable);
    }

    public Comment getComment(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
    }

    @Transactional
    public void updateComment(Account account, long commentId, String content) {
        if (!isAuthorMatched(account, commentId)) {
            throw new AuthorNotEqualException();
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Account account, long commentId) {
        if (!isAuthorMatched(account, commentId)) {
            throw new AuthorNotEqualException();
        }

        Long heartCount = heartService.getHeartCountForComment(commentId);
        if (heartCount != null && heartCount != 0) {
            heartService.deleteAllHeartForComment(commentId);
            account.decreaseCommentHeartPoint(heartCount);
        }
        commentRepository.deleteById(commentId);

        account.decreaseCommentPoint();
        account.updateLevel();
        accountRepository.save(account);
    }

    public boolean isAuthorMatched(Account account, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
        return account.getId().equals(comment.getAuthor().getId());
    }

    public Long getCommentCountForPost(long postId) {
        return commentRepository.countByPostId(postId);
    }

    public void deleteAllCommentForPost(long postId) {
        List<Comment> comments = commentRepository.findAllByPostId(postId);
        for (Comment comment : comments) {
            Account author = comment.getAuthor();
            this.deleteComment(author, comment.getId());
        }
    }
}
