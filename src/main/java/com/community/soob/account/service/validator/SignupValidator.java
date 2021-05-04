package com.community.soob.account.service.validator;

import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Component
public class SignupValidator implements Validator {
    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountSignupRequestDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountSignupRequestDto signupRequestDto = (AccountSignupRequestDto) target;

        String nickname = signupRequestDto.getNickname();
        String password = signupRequestDto.getPassword();
        String confirmPassword = signupRequestDto.getConfirmPassword();

        // 2~10 자의 한글, 영어, 숫자만 사용 가능
        NicknameUpdateValidator.validateNickname(errors, nickname);

        // 8~20 자의 한글, 영어, 숫자만 사용 가능
        PasswordUpdateValidator.validatePassword(errors, password);

        if (accountRepository.existsByEmail(signupRequestDto.getEmail())) {
            errors.rejectValue("email", "invalid.email", "이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(nickname)) {
            errors.rejectValue("nickname", "invalid.nickname", "이미 사용중인 닉네임입니다.");
        }

        if (password.equals(confirmPassword)) {
            errors.rejectValue("confirmPassword", "invalid.password", "패스워드가 일치하지 않습니다.");
        }
    }
}
