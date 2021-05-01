package com.community.soob.account.controller.dto;

import com.community.soob.account.domain.Account;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountResponseDto {
    private String email;
    private String nickname;
    private String role;
    private String hashedFileName;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    @Builder
    public AccountResponseDto(String email, String nickname, String role, String hashedFileName, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.hashedFileName = hashedFileName;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static AccountResponseDto fromEntity(Account account) {
        return AccountResponseDto.builder()
                .email(account.getEmail())
                .nickname(account.getNickname())
                .role(account.getRole().name())
                .hashedFileName(account.getProfileImage().getHashedFileName())
                .createdAt(account.getCreatedAt())
                .lastModifiedAt(account.getLastModifiedAt())
                .build();
    }
}
