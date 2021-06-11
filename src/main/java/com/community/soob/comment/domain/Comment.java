package com.community.soob.comment.domain;

import com.community.soob.account.domain.Account;
import com.community.soob.config.AuditedEntity;
import com.community.soob.post.domain.Post;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
@Entity
public class Comment extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account author;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "heart_count")
    private int heartCount;

    @Builder
    public Comment(Long id, Account author, String content, Post post, int heartCount) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.post = post;
        this.heartCount = heartCount;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void increaseHeart() {
        this.heartCount += 1;
    }

    public void decreaseHeart() {
        this.heartCount -= 1;
    }
}
