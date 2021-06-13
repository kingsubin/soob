package com.community.soob.account.controller.dto;

import com.community.soob.account.domain.Account;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountResponseDto {
    private String email;
    private String nickname;
    private int levelPoint;
    private String role;
    private String profileImagePath;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    @Builder
    public AccountResponseDto(String email, String nickname, int levelPoint, String role, String profileImagePath, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        this.email = email;
        this.nickname = nickname;
        this.levelPoint = levelPoint;
        this.role = role;
        this.profileImagePath = profileImagePath;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static AccountResponseDto fromEntity(Account account) {
        if (account.getProfileImage() == null) {
            return AccountResponseDto.builder()
                    .email(account.getEmail())
                    .nickname(account.getNickname())
                    .levelPoint(account.getLevelPoint())
                    .role(account.getRole().name())
                    .createdAt(account.getCreatedAt())
                    .lastModifiedAt(account.getLastModifiedAt())
                    .build();
        } else {
            return AccountResponseDto.builder()
                    .email(account.getEmail())
                    .nickname(account.getNickname())
                    .levelPoint(account.getLevelPoint())
                    .role(account.getRole().name())
                    .profileImagePath(account.getProfileImage().getFilePath())
                    .createdAt(account.getCreatedAt())
                    .lastModifiedAt(account.getLastModifiedAt())
                    .build();
        }
    }
}
