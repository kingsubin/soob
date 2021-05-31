package com.community.soob.account.service;

import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.exception.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    AuthServiceImpl authServiceImpl;
    SaltService saltService;

    @Mock RedisUtil redisUtil;
    @Mock AccountRepository accountRepository;
    @Mock EmailService emailService;

    private AccountSignupRequestDto createAccountDto() {
        AccountSignupRequestDto signupRequestDto = new AccountSignupRequestDto();
        signupRequestDto.setEmail("test@mail.com");
        signupRequestDto.setNickname("test");
        signupRequestDto.setPassword("password");
        signupRequestDto.setConfirmPassword("password");

        return signupRequestDto;
    }

    @BeforeEach
    void setUp() {
        String verificationDuration = "1800";
        String verificationLink = "http://localhost:8080/account/verify/";
        saltService = new SaltService();
        this.authServiceImpl = new AuthServiceImpl(
                accountRepository, emailService, saltService, redisUtil, verificationDuration, verificationLink);
    }

    // ----- 회원가입 -----
    @DisplayName("회원가입 - 성공")
    @Test
    void testSignupSuccess() {
        AccountSignupRequestDto signupRequestDto = createAccountDto();

        authServiceImpl.signup(signupRequestDto);

        verify(accountRepository).save(any());
    }

    @DisplayName("회원가입 실패 - 닉네임 정규식")
    @Test
    void testSignupFailureByInvalidNickname() {
    }

    @DisplayName("회원가입 실패 - 패스워드 정규식")
    @Test
    void testSignupFailureByInvalidPassword() {
    }

    @DisplayName("회원가입 실패 - 패스워드, 확인패스워드 불일치")
    @Test
    void testSignupFailureByNotEqualPassword() {
    }

    @DisplayName("회원가입 실패 - 이메일 중복")
    @Test
    void testSignupFailureByDuplicatedEmail() {
        AccountSignupRequestDto signupRequestDto = createAccountDto();
        when(accountRepository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authServiceImpl.signup(signupRequestDto));

        verify(accountRepository, atLeastOnce()).existsByEmail("test@mail.com");
    }

    @DisplayName("회원가입 실패 - 닉네임 중복")
    @Test
    void testSignupFailureByDuplicatedNickname() {
        AccountSignupRequestDto signupRequestDto = createAccountDto();
        when(accountRepository.existsByNickname("test")).thenReturn(true);

        assertThrows(DuplicateNicknameException.class, () -> authServiceImpl.signup(signupRequestDto));

        verify(accountRepository, atLeastOnce()).existsByNickname("test");
    }

    // ----- 로그인 -----
    @DisplayName("로그인 실패 - 패스워드 불일치")
    @Test
    void testLoginFailureByNotMatchedPassword() {
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

    // ----- 회원가입 인증 메일 보내기 -----
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

    // ----- 이메일 인증 -----
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

    // ----- 임시 패스워드 전송 -----
    @DisplayName("임시패스워드 전송 실패 - 존재하지 않는 이메일")
    @Test
    void testSendTempPasswordEmailFailureByInvalidEmail() {
    }

    @DisplayName("임시패스워드 전송 성공 - 패스워드 재설정 후 이메일 전송")
    @Test
    void testSendTempPasswordEmailSuccess() {
    }

    // ----- 패스워드 업데이트 -----
    @DisplayName("패스워드 업데이트 실패 - 패스워드 불일치")
    @Test
    void testUpdatePasswordFailureByNotMatchedPassword() {
    }

    @DisplayName("패스워드 업데이트 실패 - 패스워드 정규식")
    @Test
    void testUpdatePasswordFailureByInvalidPassword() {
    }

    @DisplayName("패스워드 업데이트 실패 - 패스워드, 확인패스워드 불일치")
    @Test
    void testUpdatePasswordFailureByNotEqualPassword() {
    }

    @DisplayName("패스워드 업데이트 성공 - 패스워드 일치")
    @Test
    void testUpdatePasswordSuccess() {
    }
}
