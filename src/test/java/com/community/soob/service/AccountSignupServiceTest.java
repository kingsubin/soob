package com.community.soob.service;

import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.service.*;
import com.community.soob.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccountSignupServiceTest {
    private AccountSignupService accountSignupService;
    @Mock private AccountRepository accountRepository;
    @Mock private EmailService emailService;
    @Mock private AccountCheckService accountCheckService;
    @Mock private AccountFindService accountFindService;
    @Mock private SaltService saltService;
    @Mock private RedisUtil redisUtil;

    @BeforeEach
    void setUp() {
        String verificationDuration = "1800";
        String verificationLink = "http://localhost:8080/account/verify/";
        this.accountSignupService = new AccountSignupService(
                accountRepository, emailService, accountCheckService, accountFindService, saltService, redisUtil, verificationDuration, verificationLink);
    }

    @DisplayName("회원가입 성공")
    @Test
    void testSignupSuccess() {
        // given
        AccountSignupRequestDto signupRequestDto = new AccountSignupRequestDto();
        signupRequestDto.setEmail("test@mail.com");
        signupRequestDto.setNickname("test");
        signupRequestDto.setPassword("password1234!@#$");
        signupRequestDto.setConfirmPassword("password1234!@#$");

        // when
        accountSignupService.signup(signupRequestDto);

        // then
        then(accountRepository).should().save(any());
    }

    @DisplayName("회원가입 인증 메일 보내기 성공")
    @Test
    void testSendSignupVerificationEmailSuccess() {
        // given
        String email = "testEmail@gmail.com";

        // when
        accountSignupService.sendSignupVerificationEmail(email);

        // then
        then(emailService).should().sendEmail(eq(email), any(), any());
    }

    @DisplayName("이메일 인증 실패 - Redis 에 email Key 존재하지 않음")
    @Test
    void testVerifyEmailFailureByInvalidRedisKey() {
        // given
        String key = "InvalidKey";

        // when
        // then
        assertNull(redisUtil.getData(key));
    }

    @DisplayName("이메일 인증 성공")
    @Test
    void testVerifyEmailSuccess() {
        // given
        String key = "eyJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50RW1haWwiOiJraW5nc3ViaW5AbmF2ZXIuY29tIiwiaWF0IjoxNjIwMTI3NjA1LCJleHAiOjE2MjAxMzA0ODV9.zOiWpqoVX3R_EjeV1DL1lQwlZFnSec4RrFRQuK-IZV4";
        given(redisUtil.getData(key)).willReturn("kingsubin@naver.com");

        Account account = Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .levelPoint(50)
                .role(Role.LEVEL_1)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();
        given(accountFindService.findByEmail("kingsubin@naver.com"))
                .willReturn(account);

        // when
        accountSignupService.verifyEmail(key);

        // then
        assertEquals(Role.LEVEL_1, account.getRole());
        then(redisUtil).should().deleteData(key);
    }
}
