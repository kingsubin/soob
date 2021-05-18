package com.community.soob.heart.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartRepository extends JpaRepository<Heart, Long> {
    Heart findByPostIdAndAccountId(Long postId, Long accountIdd);
    Heart findByCommentIdAndAccountId(Long commentId, Long accountId);
    Long countByPostId(Long postId);
    Long countByCommentId(Long commentId);

    void deleteAllByPostId(long postId);
    void deleteAllByCommentId(long commentId);
}
