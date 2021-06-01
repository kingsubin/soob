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
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    AuthServiceImpl authServiceImpl;

    @Spy
    SaltService saltService = new SaltService();

    @Mock RedisUtil redisUtil;
    @Mock AccountRepository accountRepository;
    @Mock EmailService emailService;

    private UUID defaultUUID = UUID.fromString("d49de159-7c60-41bb-9f4c-51ba1087f696");

    private AccountSignupRequestDto createSignupRequestDto() {
        AccountSignupRequestDto signupRequestDto = new AccountSignupRequestDto();
        signupRequestDto.setEmail("test@mail.com");
        signupRequestDto.setNickname("test");
        signupRequestDto.setPassword("password1234!@#$");
        signupRequestDto.setConfirmPassword("password1234!@#$");

        return signupRequestDto;
    }

    private Account createAccount() {
        return Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .role(Role.NOT_PERMITTED)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();
    }

    @BeforeEach
    void setUp() {
        String verificationDuration = "1800";
        String verificationLink = "http://localhost:8080/account/verify/";
        this.authServiceImpl = new AuthServiceImpl(
                accountRepository, emailService, saltService, redisUtil, verificationDuration, verificationLink);
    }

    // ----- 회원가입 -----
    @DisplayName("회원가입 - 성공")
    @Test
    void testSignupSuccess() {
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();

        authServiceImpl.signup(signupRequestDto);

        verify(accountRepository).save(any());
    }

    @DisplayName("회원가입 실패 - 이메일 정규식")
    @Test
    void testSignupFailureByInvalidEmail() {
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setEmail("InvalidEmail!@#");
        AuthServiceImpl authService = spy(authServiceImpl);

        assertThrows(InvalidEmailException.class, () -> authService.signup(signupRequestDto));

        verify(authService).checkEmailRegex(signupRequestDto.getEmail());
    }

    @DisplayName("회원가입 실패 - 닉네임 정규식")
    @Test
    void testSignupFailureByInvalidNickname() {
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setNickname("InvalidNickname!@#");
        AuthServiceImpl authService = spy(authServiceImpl);

        assertThrows(InvalidNicknameException.class, () -> authService.signup(signupRequestDto));

        verify(authService).checkNicknameRegex(signupRequestDto.getNickname());
    }

    @DisplayName("회원가입 실패 - 패스워드 정규식")
    @Test
    void testSignupFailureByInvalidPassword() {
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setPassword("InvalidPass");
        signupRequestDto.setConfirmPassword("InvalidPass");
        AuthServiceImpl authService = spy(authServiceImpl);

        assertThrows(InvalidPasswordException.class, () -> authService.signup(signupRequestDto));

        verify(authService).checkPasswordRegex(signupRequestDto.getPassword());
    }

    @DisplayName("회원가입 실패 - 패스워드, 확인패스워드 불일치")
    @Test
    void testSignupFailureByNotEqualPassword() {
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setConfirmPassword("NotEqualPassword");
        AuthServiceImpl authService = spy(authServiceImpl);

        assertThrows(AccountPasswordNotMatchedException.class, () -> authService.signup(signupRequestDto));

        verify(authService).checkPasswordMatching(signupRequestDto.getPassword(), signupRequestDto.getConfirmPassword());
    }

    @DisplayName("회원가입 실패 - 이메일 중복")
    @Test
    void testSignupFailureByDuplicatedEmail() {
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        when(accountRepository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authServiceImpl.signup(signupRequestDto));

        verify(accountRepository, atLeastOnce()).existsByEmail("test@mail.com");
    }

    @DisplayName("회원가입 실패 - 닉네임 중복")
    @Test
    void testSignupFailureByDuplicatedNickname() {
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        when(accountRepository.existsByNickname("test")).thenReturn(true);

        assertThrows(DuplicateNicknameException.class, () -> authServiceImpl.signup(signupRequestDto));

        verify(accountRepository, atLeastOnce()).existsByNickname("test");
    }

    // ----- 로그인 -----
    @DisplayName("로그인 실패 - 패스워드 불일치")
    @Test
    void testLoginFailureByNotMatchedPassword() {
        AccountLoginRequestDto loginRequestDto = new AccountLoginRequestDto();
        loginRequestDto.setEmail("test@test.com");
        loginRequestDto.setPassword("failPassword");

        Account account = createAccount();
        when(accountRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(account));

        assertThrows(AccountPasswordNotMatchedException.class, () -> {
            authServiceImpl.login(loginRequestDto);
        });

        verify(accountRepository).findByEmail(loginRequestDto.getEmail());
        verify(saltService).matches(loginRequestDto.getPassword(), account.getPassword());
    }

    @DisplayName("로그인 실패 - 존재하지않는 이메일")
    @Test
    void testLoginFailureByInvalidEmail() {
        AccountLoginRequestDto loginRequestDto = new AccountLoginRequestDto();
        loginRequestDto.setEmail("failEmail@test.com");
        loginRequestDto.setPassword("password");

        when(accountRepository.findByEmail(loginRequestDto.getEmail()))
                .thenThrow(new AccountNotFoundException());

        assertThrows(AccountNotFoundException.class, () -> {
            authServiceImpl.login(loginRequestDto);
        });

        verify(accountRepository).findByEmail(loginRequestDto.getEmail());
    }

    @DisplayName("로그인 성공 - 이메일, 패스워드 일치")
    @Test
    void testLoginSuccess() {
        AccountLoginRequestDto loginRequestDto = new AccountLoginRequestDto();
        loginRequestDto.setEmail("test@test.com");
        loginRequestDto.setPassword("password");

        Account account = createAccount();
        when(accountRepository.findByEmail(loginRequestDto.getEmail()))
                .thenReturn(Optional.of(account));

        authServiceImpl.login(loginRequestDto);

        verify(accountRepository).findByEmail(loginRequestDto.getEmail());
        verify(saltService).matches(loginRequestDto.getPassword(), account.getPassword());
    }

    // ----- 회원가입 인증 메일 보내기 -----
    @DisplayName("회원가입 인증 메일 보내기 성공 - 유효한 이메일")
    @Test
    void testSendSignupVerificationEmailSuccess() {
        String email = "testEmail@gmail.com";

        authServiceImpl.sendSignupVerificationEmail(email);

        verify(emailService).sendEmail(eq(email), any(), any());
    }

    @DisplayName("회원가입 인증 메일 보내기 실패 - 유효하지 않은 이메일")
    @Test
    void testSendSignupVerificationEmailFailureByInvalidEmail() {
        String email = "InvalidEmail";
        AuthServiceImpl authService = spy(authServiceImpl);

        assertThrows(InvalidEmailException.class, () -> {
            authService.sendSignupVerificationEmail(email);
        });

        verify(authService).checkEmailRegex(email);
    }

    // ----- 이메일 인증 -----
    @DisplayName("이메일 인증 실패 - Redis 에 email Key 존재하지 않음")
    @Test
    void testVerifyEmailFailureByInvalidRedisKey() {
        String key = "InvalidKey";

        assertNull(redisUtil.getData(key));

        verify(redisUtil).getData(key);
    }

    @DisplayName("이메일 인증 실패 - 존재하지 않는 이메일")
    @Test
    void testVerifyEmailFailureByInvalidEmail() {
        String key = "eyJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50RW1haWwiOiJxdWVlbnN1YmluQG5hdmVyLmNvbSIsImlhdCI6MTYyMDcxMDc2OCwiZXhwIjoxNjIwNzIwODQ4fQ.V0RTILEB72nCraqRO_OhTIXzzlQ9CxHlkyheyfaVDVU";
        String email = "";
        when(redisUtil.getData(key)).thenReturn(email = "queensubin@naver.com");

        assertThrows(AccountNotFoundException.class, () -> authServiceImpl.verifyEmail(key));

        verify(redisUtil).getData(key);
        verify(accountRepository).findByEmail(email);
    }

    @DisplayName("이메일 인증 성공 - 이메일, Redis")
    @Test
    void testVerifyEmailSuccess() {
        String key = "eyJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50RW1haWwiOiJraW5nc3ViaW5AbmF2ZXIuY29tIiwiaWF0IjoxNjIwMTI3NjA1LCJleHAiOjE2MjAxMzA0ODV9.zOiWpqoVX3R_EjeV1DL1lQwlZFnSec4RrFRQuK-IZV4";
        when(redisUtil.getData(key)).thenReturn("kingsubin@naver.com");

        Account account = createAccount();
        when(accountRepository.findByEmail("kingsubin@naver.com"))
                .thenReturn(Optional.of(account));

        authServiceImpl.verifyEmail(key);

        assertEquals(account.getRole(), Role.LEVEL_1);

        verify(redisUtil).deleteData(key);
    }

    // ----- 임시 패스워드 전송 -----
    @DisplayName("임시패스워드 전송 실패 - 존재하지 않는 이메일")
    @Test
    void testSendTempPasswordEmailFailureByInvalidEmail() {
        String email = "InvalidEmail";

        assertThrows(AccountNotFoundException.class, () -> {
            authServiceImpl.sendTempPasswordEmail(email);
        });

        verify(accountRepository).findByEmail(email);
    }

    @DisplayName("임시패스워드 전송 성공 - 패스워드 재설정 후 이메일 전송")
    @Test
    void testSendTempPasswordEmailSuccess() {
        String email = "kingsubin@naver.com";
        Account account = createAccount();
        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        MockedStatic<UUID> mockedUUID = mockStatic(UUID.class);
        mockedUUID.when(UUID::randomUUID).thenReturn(defaultUUID);

        String salt = "";
        String saltingPassword = "";
        doReturn(salt = "$2a$10$MIGfGk4v0EyGdlLOZL.H8O").when(saltService).genSalt();
        doReturn(saltingPassword = "$2a$10$MIGfGk4v0EyGdlLOZL.H8OLQDM4OeWLC4ql2RHRY/hpLbis7l60Dq").when(saltService).encodePassword(salt, defaultUUID.toString());

        authServiceImpl.sendTempPasswordEmail(email);

        assertEquals(account.getSalt(), salt);
        assertEquals(account.getPassword(), saltingPassword);
        verify(accountRepository).findByEmail(email);
        verify(emailService).sendEmail(eq(email), any(), any());
        verify(accountRepository).save(account);
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
