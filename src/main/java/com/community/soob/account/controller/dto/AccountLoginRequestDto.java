package com.community.soob.account.controller.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class AccountLoginRequestDto {
    @Email(message = "이메일 양식을 지켜주세요.")
    @NotBlank(message = "이메일을 작성해주세요.")
    private String email;

    @NotBlank(message = "패스워드를 작성해주세요.")
    private String password;
}
