package com.community.soob.account.controller.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class AccountUpdateRequestDto {
    @NotBlank(message = "닉네임을 작성해주세요.")
    @Length(min = 2, max = 10, message = "2~10 자로 작성해주세요.")
    private String nickname;
}
