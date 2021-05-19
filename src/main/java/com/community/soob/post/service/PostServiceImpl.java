package com.community.soob.post.service;

import com.community.soob.account.domain.Account;
import com.community.soob.comment.service.CommentService;
import com.community.soob.heart.service.HeartService;
import com.community.soob.post.domain.Board;
import com.community.soob.post.domain.BoardRepository;
import com.community.soob.post.domain.Post;
import com.community.soob.post.domain.PostRepository;
import com.community.soob.post.exception.BoardNotFoundException;
import com.community.soob.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final CommentService commentService;
    private final HeartService heartService;

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
    public Post createPost(Account account, long boardId, String title, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);
        Post post = Post.builder()
                .board(board)
                .author(account)
                .title(title)
                .content(content)
                .build();
        return postRepository.save(post);
    }

    @Transactional
    @Override
    public void updatePost(Post post, String title, String content) {
        post.updatePost(title, content);
        postRepository.save(post);
    }

    @Transactional
    @Override
    public void deletePost(long postId) {
        Long heartCount = heartService.getHeartCountForPost(postId);
        if (heartCount != null && heartCount != 0) {
            heartService.deleteAllHeartForPost(postId);
        }

        Long commentCount = commentService.getCommentCountForPost(postId);
        if (commentCount != null && commentCount != 0) {
            commentService.deleteAllCommentForPost(postId);
        }

        postRepository.deleteById(postId);
    }

    @Override
    public boolean isAuthorMatched(Account account, long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        return account.getId().equals(post.getAuthor().getId());
    }
}
