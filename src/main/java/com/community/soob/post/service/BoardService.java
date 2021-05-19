package com.community.soob.post.service;

import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class BoardService {
    private final PostRepository postRepository;

    public Page<Post> getPostsByBoardId(long boardId, Pageable pageable) {
        pageable = PageRequest.of((pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1), 10);
        return postRepository.findAllByBoardId(boardId, pageable);
    }
}
