package com.community.soob.post.domain;

import com.community.soob.account.domain.Account;
import com.community.soob.attachment.Attachment;
import com.community.soob.config.AuditedEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
@Entity
public class Post extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account author;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @Column(name = "read_count")
    private int readCount;

    @Column(name = "heart_count")
    private int heartCount;

    @Builder
    public Post(Long id, Board board, Account author, String title, String content, List<Attachment> attachments, int readCount, int heartCount) {
        this.id = id;
        this.board = board;
        this.author = author;
        this.title = title;
        this.content = content;
        this.attachments = attachments;
        this.readCount = readCount;
        this.heartCount = heartCount;
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void increaseReadCount() {
        this.readCount++;
    }

    public void increaseHeart() {
        this.heartCount++;
    }

    public void decreaseHeart() {
        this.heartCount--;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setPost(this);
    }

    public void removeAttachment(Attachment attachment){
        attachments.remove(attachment);
        attachment.setPost(null);
    }
}
