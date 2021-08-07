package com.community.soob.post.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 파라미터로 받아오는 보드번호에 해당하는 게시글 전체 조회
    Page<Post> findAllByBoardId(long boardId, Pageable pageable);

    // 파라미터로 받아오는 보드번호에 해당하는 게시글 5개 조회
    List<Post> findTop5ByBoardIdOrderByLastModifiedAt(long boardId);
}
