package com.community.soob.account.domain;

import com.community.soob.attachment.Attachment;
import com.community.soob.config.AuditedEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account")
@Entity
public class Account extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    private String salt;

    @Column(name = "level_point")
    private int levelPoint;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attachment_id")
    private Attachment profileImage;

    @Builder
    public Account(Long id, String email, String password, String nickname, String salt, int levelPoint, Role role, Attachment profileImage) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.salt = salt;
        this.levelPoint = levelPoint;
        this.role = role;
        this.profileImage = profileImage;
    }

    public void updateEmailVerified() {
        this.role = Role.LEVEL_1;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(Attachment profileImage) {
        this.profileImage = profileImage;
    }

    public void updatePassword(String salt, String saltingPassword) {
        this.salt = salt;
        this.password = saltingPassword;
    }
}
