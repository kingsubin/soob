package com.community.soob.account.service;

import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.controller.dto.AccountPasswordUpdateRequestDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;

public interface AuthService {
    void signup(AccountSignupRequestDto signupRequestDto);
    Account login(AccountLoginRequestDto loginRequestDto);
    void sendSignupVerificationEmail(String email);
    void verifyEmail(String key);
    void sendTempPasswordEmail(String email);
    void updatePassword(Account account, AccountPasswordUpdateRequestDto passwordUpdateRequestDto);

    boolean checkEmailDuplicated(String email);
    boolean checkNicknameDuplicated(String nickname);
    void checkEmailRegex(String email);
    void checkNicknameRegex(String nickname);
    void checkPasswordRegex(String password);
    void checkPasswordMatching(String password, String confirmPassword);
}
