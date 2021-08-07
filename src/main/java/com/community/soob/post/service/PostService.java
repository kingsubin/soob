package com.community.soob.post.service;

import com.community.soob.account.domain.Account;
import com.community.soob.post.domain.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    Post getPost(long postId);
    void createPost(Account account, long boardId, String title, String content, List<MultipartFile> files);
    void updatePost(Account account, long postId, String title, String content, List<MultipartFile> files);
    void deletePost(Account account, long postId);
    boolean isAuthorMatched(Account account, long postId);
}
