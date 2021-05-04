package com.community.soob.account.service.validator;

import com.community.soob.account.controller.dto.AccountNicknameUpdateRequestDto;
import com.community.soob.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Component
public class NicknameUpdateValidator implements Validator {
    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountNicknameUpdateRequestDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountNicknameUpdateRequestDto nicknameUpdateRequestDto = (AccountNicknameUpdateRequestDto) target;
        String nickname = nicknameUpdateRequestDto.getNickname();

        // 2~10 자의 한글, 영어, 숫자만 사용 가능
        validateNickname(errors, nickname);

        if (accountRepository.existsByNickname(nickname)) {
            errors.rejectValue("nickname", "invalid.nickname", "이미 사용중인 닉네임입니다.");
        }
    }

    static void validateNickname(Errors errors, String nickname) {
        for (int i = 0; i < nickname.length(); i++) {
            char c = nickname.charAt(i);
            if (!Character.isDigit(c) || !Character.isLetter(c)) {
                errors.rejectValue("nickname", "invalid.nickname", "한글, 영어, 숫자만 사용 가능합니다.");
                break;
            }
        }
    }
}
