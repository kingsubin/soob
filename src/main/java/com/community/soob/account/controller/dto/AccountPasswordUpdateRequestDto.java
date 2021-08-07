package com.community.soob.account.controller.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class AccountPasswordUpdateRequestDto {
    @NotBlank(message = "현재 패스워드를 작성해주세요.")
    private String currentPassword;

    @NotBlank(message = "변경할 패스워드를 작성해주세요.")
    @Length(min = 12, message = "12자 이상으로 작성해주세요.")
    private String newPassword;

    @NotBlank(message = "설정한 패스워드와 일치시켜주세요.")
    @Length(min = 12, message = "패스워드와 일치 시켜주세요.")
    private String confirmNewPassword;
}
