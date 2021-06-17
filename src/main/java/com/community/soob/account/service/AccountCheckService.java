package com.community.soob.account.service;

import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import com.community.soob.account.exception.InvalidEmailException;
import com.community.soob.account.exception.InvalidNicknameException;
import com.community.soob.account.exception.InvalidPasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountCheckService {
    private final AccountRepository accountRepository;

    public boolean checkEmailDuplicated(String email) {
        return accountRepository.existsByEmail(email);
    }

    public boolean checkNicknameDuplicated(String nickname) {
        return accountRepository.existsByNickname(nickname);
    }

    public void checkEmailRegex(String email) {
        // 이메일 형식 체크
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(regex, email)) {
            throw new InvalidEmailException();
        }
    }

    public void checkNicknameRegex(String nickname) {
        // 문자, 숫자만 가능
        for (int i = 0; i < nickname.length(); i++) {
            char c = nickname.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                throw new InvalidNicknameException();
            }
        }
    }

    public void checkPasswordRegex(String password) {
        // 문자, 숫자, 특수문자 12자 이상
        String regex = "^[a-zA-Z0-9~!@#$%^&*()_+=.-]{12,}$";
        if (!Pattern.matches(regex, password)) {
            throw new InvalidPasswordException();
        }
    }

    public void checkPasswordMatching(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new AccountPasswordNotMatchedException();
        }
    }
}
