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
    @Mock
    AccountRepository accountRepository;

    @Mock
    EmailService emailService;

    // Mock 일때는작동안하는거니까 그냥 새 객체를 만들어줌
    SaltService saltService = new SaltService();

    @Mock
    RedisUtil redisUtil;

    AuthServiceImpl authServiceImpl;

    @BeforeEach
    void setUp() {
        this.authServiceImpl = new AuthServiceImpl(
                accountRepository, emailService, saltService, redisUtil, "1000", "1000");
    }

    @DisplayName("회원가입성공")
    @Test
    void testSignupSuccess() {
        AccountSignupRequestDto signupRequestDto = new AccountSignupRequestDto();
        signupRequestDto.setEmail("test@naver.com");
        signupRequestDto.setNickname("test");
        signupRequestDto.setPassword("password");
        signupRequestDto.setConfirmPassword("password");

        authServiceImpl.signup(signupRequestDto);

        // 디비에 잘 들어오는가 ?
        // any() -> 아무거나 들어와도 메소드가 호출되는지만 확인
        verify(accountRepository).save(any());
    }

    @DisplayName("회원가입 유효성 검사 실패 - 잘못된 이메일")
    @Test
    void testSignupFailureByInvalidEmail() {
    }

    @DisplayName("패스워드 불일치")
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

    @DisplayName("이메일 존재하지않음")
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

    @DisplayName("성공시 토큰이 제대로 발급되고 쿠키에 들어오는지")
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
                .nickname("킹수빈")
                .role(Role.LEVEL_1)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();

        when(accountRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(account));

        authServiceImpl.login(loginRequestDto);

        verify(accountRepository).findByEmail("test@test.com");
    }


    @Test
    void sendSignupVerificationEmail() {
        String email = "binch1226@naver.com";

        authServiceImpl.sendSignupVerificationEmail(email);

        verify(emailService).sendEmail(eq(email), any(), any());
    }

    @Test
    void verifyEmail() {
    }

    @Test
    void sendTempPasswordEmail() {
    }

    @Test
    void updatePassword() {
    }
}
