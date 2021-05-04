package com.community.soob.account.service.validator;

import com.community.soob.account.controller.dto.AccountPasswordUpdateRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PasswordUpdateValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return AccountPasswordUpdateRequestDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountPasswordUpdateRequestDto passwordUpdateRequestDto = (AccountPasswordUpdateRequestDto) target;
        String newPassword = passwordUpdateRequestDto.getNewPassword();
        String confirmPassword = passwordUpdateRequestDto.getConfirmNewPassword();

        if (!newPassword.equals(confirmPassword)) {
            errors.rejectValue("confirmPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }

        // 8~20 자의 한글, 영어, 숫자만 사용 가능
        validatePassword(errors, newPassword);
    }

    static void validatePassword(Errors errors, String password) {
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (!Character.isDigit(c) || !Character.isLetter(c)) {
                errors.rejectValue("newPassword", "invalid.password","한글, 영어, 숫자만 사용 가능합니다.");
                break;
            }
        }
    }
}
