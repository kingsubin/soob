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

    void checkRegex(String str);
    void checkPasswordMatching(String password, String confirmPassword);
}
