package com.community.soob.account.controller.dto;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class AccountSignupRequestDto {
    @Email(message = "이메일 양식을 지켜주세요.")
    @NotBlank(message = "이메일을 작성해주세요.")
    private String email;

    @NotBlank(message = "닉네임을 작성해주세요.")
    @Length(min = 2, max = 10, message = "2~10 자로 작성해주세요.")
    private String nickname;

    @NotBlank(message = "패스워드를 작성해주세요.")
    @Length(min = 12, message = "12자 이상으로 작성해주세요.")
    private String password;

    @NotBlank(message = "설정한 패스워드와 일치시켜주세요.")
    @Length(min = 12, message = "패스워드와 일치 시켜주세요.")
    private String confirmPassword;

    @JsonIgnore
    private String salt;

    public Account toEntity() {
        return Account.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .role(Role.NOT_PERMITTED)
                .salt(salt)
                .build();
    }
}
