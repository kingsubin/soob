package com.community.soob.comment.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByPostId(long postId, Pageable pageable);
    List<Comment> findAllByPostId(long postId);
    Long countByPostId(long postId);
}
