package com.community.soob.heart.service;

import com.community.soob.account.domain.Account;
import com.community.soob.comment.domain.Comment;
import com.community.soob.comment.domain.CommentRepository;
import com.community.soob.comment.exception.CommentNotFoundException;
import com.community.soob.heart.domain.Heart;
import com.community.soob.heart.domain.HeartRepository;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import com.community.soob.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class HeartService {
    private final HeartRepository heartRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void toggleHeartToPost(Account account, long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        Heart heart = Heart.builder()
                .account(account)
                .post(post)
                .build();

        Heart foundHeart = heartRepository.findByPostIdAndAccountId(postId, account.getId());
        if (foundHeart == null) {
            heartRepository.save(heart);
            post.increaseHeart();
        } else {
            heartRepository.delete(foundHeart);
            post.decreaseHeart();
        }

        postRepository.save(post);
    }

    @Transactional
    public void toggleHeartToComment(Account account, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
        Heart heart = Heart.builder()
                .account(account)
                .comment(comment)
                .build();

        Heart foundHeart = heartRepository.findByCommentIdAndAccountId(commentId, account.getId());
        if (foundHeart == null) {
            heartRepository.save(heart);
            comment.increaseHeart();
        } else {
            heartRepository.delete(foundHeart);
            comment.decreaseHeart();
        }

        commentRepository.save(comment);
    }

    public Long getHeartCountForPost(long postId) {
        return heartRepository.countByPostId(postId);
    }

    public Long getHeartCountForComment(long commentId) {
        return heartRepository.countByCommentId(commentId);
    }

    @Transactional
    public void deleteAllHeartForPost(long postId) {
        heartRepository.deleteAllByPostId(postId);
    }

    @Transactional
    public void deleteAllHeartForComment(long commentId) {
        heartRepository.deleteAllByCommentId(commentId);
    }
}
