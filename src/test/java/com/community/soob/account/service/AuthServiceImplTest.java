package com.community.soob.account.service;

import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.exception.AccountNotFoundException;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import com.community.soob.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock AccountRepository accountRepository;
    @Mock EmailService emailService;
    @Mock RedisUtil redisUtil;

    // SaltService 의 경우 Mock 일때는 작동 안하는거니까 그냥 새 객체를 만들어줌
    SaltService saltService = new SaltService();
    AuthServiceImpl authServiceImpl;

    @BeforeEach
    void setUp() {
        this.authServiceImpl = new AuthServiceImpl(
                accountRepository, emailService, saltService, redisUtil, "1000", "1000");
    }

    @DisplayName("회원가입 - 성공")
    @Test
    void testSignupSuccess() {
        AccountSignupRequestDto signupRequestDto = new AccountSignupRequestDto();
        signupRequestDto.setEmail("test@naver.com");
        signupRequestDto.setNickname("test");
        signupRequestDto.setPassword("password");
        signupRequestDto.setConfirmPassword("password");

        authServiceImpl.signup(signupRequestDto);

        // AccountRepository 에서 save 메소드를 호출하는가 ?
        verify(accountRepository).save(any());
    }

    @DisplayName("회원가입 실패 - 이메일 유효성 검사")
    @Test
    void testSignupFailureByInvalidEmail() {

    }

    @DisplayName("로그인 실패 - 유효하지 않은 패스워드")
    @Test
    void testLoginFailureByInvalidPassword() {
        // test@test.com
        // password
        AccountLoginRequestDto loginRequestDto = new AccountLoginRequestDto();
        loginRequestDto.setEmail("test@test.com");
        loginRequestDto.setPassword("failPassword");

        // 디비에 있어야하는 실제 데이터인데 가짜로 만든거
        Account account = Account.builder()
                .id(1L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("킹수빈")
                .role(Role.LEVEL_1)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();

        when(accountRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(account));

        assertThrows(AccountPasswordNotMatchedException.class, () -> {
            authServiceImpl.login(loginRequestDto);
        });
    }

    @DisplayName("로그인 실패 - 존재하지않는 이메일")
    @Test
    void testLoginFailureByInvalidEmail() {
        // test@test.com
        // password
        AccountLoginRequestDto loginRequestDto = new AccountLoginRequestDto();
        loginRequestDto.setEmail("failEmail@test.com");
        loginRequestDto.setPassword("password");

        when(accountRepository.findByEmail("failEmail@test.com"))
                .thenThrow(new AccountNotFoundException());

        assertThrows(AccountNotFoundException.class, () -> {
            authServiceImpl.login(loginRequestDto);
        });
    }

    @DisplayName("로그인 성공 - 이메일, 패스워드 일치")
    @Test
    void testLoginSuccess() {
        // test@test.com
        // password
        AccountLoginRequestDto loginRequestDto = new AccountLoginRequestDto();
        loginRequestDto.setEmail("test@test.com");
        loginRequestDto.setPassword("password");

        Account account = Account.builder()
                .id(1L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.LEVEL_1)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();

        when(accountRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(account));

        authServiceImpl.login(loginRequestDto);

        verify(accountRepository).findByEmail("test@test.com");
    }

    @DisplayName("회원가입 인증 메일 보내기 성공 - 유효한 이메일")
    @Test
    void testSendSignupVerificationEmailSuccess() {
        String email = "binch1226@naver.com";

        authServiceImpl.sendSignupVerificationEmail(email);

        verify(emailService).sendEmail(eq(email), any(), any());
    }

    @DisplayName("회원가입 인증 메일 보내기 실패 - 유효하지 않은 이메일")
    @Test
    void testSendSignupVerificationEmailFailureByInvalidEmail() {

    }

    @DisplayName("이메일 인증 실패 - Redis 에 email Key 존재하지 않음")
    @Test
    void testVerifyEmailFailureByInvalidRedisKey() {

    }

    @DisplayName("이메일 인증 실패 - 유효하지 않은 이메일")
    @Test
    void testVerifyEmailFailureByInvalidEmail() {

    }

    @DisplayName("이메일 인증 성공 - 이메일, Redis")
    @Test
    void testVerifyEmailSuccess() {

    }

    @DisplayName("임시패스워드 전송 실패 - 이메일 전송")
    @Test
    void testSendTempPasswordEmailFailureBySendEmail() {
    }

    @DisplayName("임시패스워드 전송 실패 - 패스워드 재설정")
    @Test
    void testSendTempPasswordEmailFailureByResettingPassword() {
    }

    @DisplayName("임시패스워드 전송 성공 - 패스워드 재설정 후 이메일 전송")
    @Test
    void testSendTempPasswordEmailSuccess() {
    }

    @DisplayName("패스워드 업데이트 실패 - 패스워드 불일치")
    @Test
    void testUpdatePasswordFailureByInvalidPassword() {
    }

    @DisplayName("패스워드 업데이트 성공 - 패스워드 일치")
    @Test
    void testUpdatePasswordSuccess() {
    }
}
