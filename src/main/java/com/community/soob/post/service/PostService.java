package com.community.soob.post.service;

import com.community.soob.account.domain.Account;
import com.community.soob.post.domain.Post;

public interface PostService {
    Post getPost(long postId);
    Post createPost(Account account, long boardId, String title, String content);
    void updatePost(Post post, String title, String content);
    void deletePost(long postId);
    boolean isAuthorMatched(Account account, long postId);
}
