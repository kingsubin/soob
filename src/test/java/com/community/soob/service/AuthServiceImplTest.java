package com.community.soob.service;

import com.community.soob.account.controller.dto.AccountPasswordUpdateRequestDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.exception.*;
import com.community.soob.account.service.AuthServiceImpl;
import com.community.soob.account.service.EmailService;
import com.community.soob.account.service.SaltService;
import com.community.soob.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    private AuthServiceImpl authServiceImpl;
    private SaltService saltService = new SaltService();

    @Mock private RedisUtil redisUtil;
    @Mock private AccountRepository accountRepository;
    @Mock private EmailService emailService;

    private final UUID defaultUUID = UUID.fromString("d49de159-7c60-41bb-9f4c-51ba1087f696");

    private AccountSignupRequestDto createSignupRequestDto() {
        AccountSignupRequestDto signupRequestDto = new AccountSignupRequestDto();
        signupRequestDto.setEmail("test@mail.com");
        signupRequestDto.setNickname("test");
        signupRequestDto.setPassword("password1234!@#$");
        signupRequestDto.setConfirmPassword("password1234!@#$");
        return signupRequestDto;
    }

    private AccountPasswordUpdateRequestDto createPasswordUpdateRequestDto() {
        AccountPasswordUpdateRequestDto passwordUpdateRequestDto = new AccountPasswordUpdateRequestDto();
        passwordUpdateRequestDto.setCurrentPassword("password");
        passwordUpdateRequestDto.setNewPassword("newPassword123!");
        passwordUpdateRequestDto.setConfirmNewPassword("newPassword123!");
        return passwordUpdateRequestDto;
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
        // given
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();

        // when
        authServiceImpl.signup(signupRequestDto);

        // then
        then(accountRepository).should().save(any());
    }

    @DisplayName("회원가입 실패 - 이메일 정규식")
    @Test
    void testSignupFailureByInvalidEmail() {
        // given
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setEmail("InvalidEmail!@#");

        // when
        // then
        assertThrows(InvalidEmailException.class,
                () -> authServiceImpl.signup(signupRequestDto)
        );
    }

    @DisplayName("회원가입 실패 - 닉네임 정규식")
    @Test
    void testSignupFailureByInvalidNickname() {
        // given
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setNickname("InvalidNickname!@#");

        // when
        // then
        assertThrows(InvalidNicknameException.class,
                () -> authServiceImpl.signup(signupRequestDto)
        );
    }

    @DisplayName("회원가입 실패 - 패스워드 정규식")
    @Test
    void testSignupFailureByInvalidPassword() {
        // given
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setPassword("Invalid!2#");
        signupRequestDto.setConfirmPassword("Invalid!2#");

        // when
        // then
        assertThrows(InvalidPasswordException.class,
                () -> authServiceImpl.signup(signupRequestDto)
        );
    }

    @DisplayName("회원가입 실패 - 패스워드, 확인패스워드 불일치")
    @Test
    void testSignupFailureByNotEqualPassword() {
        // given
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        signupRequestDto.setConfirmPassword("NotEqualPassword");

        // when
        // then
        assertThrows(AccountPasswordNotMatchedException.class,
                () -> authServiceImpl.signup(signupRequestDto)
        );
    }

    @DisplayName("회원가입 실패 - 이메일 중복")
    @Test
    void testSignupFailureByDuplicatedEmail() {
        // given
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        given(accountRepository.existsByEmail("test@mail.com")).willReturn(true);

        // when
        // then
        assertThrows(DuplicateEmailException.class,
                () -> authServiceImpl.signup(signupRequestDto)
        );
    }

    @DisplayName("회원가입 실패 - 닉네임 중복")
    @Test
    void testSignupFailureByDuplicatedNickname() {
        // given
        AccountSignupRequestDto signupRequestDto = createSignupRequestDto();
        given(accountRepository.existsByNickname("test")).willReturn(true);

        // when
        // then
        assertThrows(DuplicateNicknameException.class,
                () -> authServiceImpl.signup(signupRequestDto)
        );
    }

    // ----- 로그인 -----
    @DisplayName("로그인 실패 - 패스워드 불일치")
    @Test
    void testLoginFailureByNotMatchedPassword() {
        // given
        String email = "test@test.com";
        String password = "failPassword";
        Account account = createAccount();

        given(accountRepository.findByEmail(email))
                .willReturn(Optional.of(account));

        // when
        // then
        assertThrows(AccountPasswordNotMatchedException.class, () ->
                authServiceImpl.login(email, password)
        );
    }

    @DisplayName("로그인 실패 - 존재하지않는 이메일")
    @Test
    void testLoginFailureByInvalidEmail() {
        // given
        String email = "failEmail@test.com";
        String password = "password";

        // when
        // then
        assertThrows(AccountNotFoundException.class,
                () -> authServiceImpl.login(email, password)
        );
    }

    @DisplayName("로그인 성공 - 이메일, 패스워드 일치")
    @Test
    void testLoginSuccess() {
        // given
        String email = "test@test.com";
        String password = "password";
        Account account = createAccount();

        given(accountRepository.findByEmail(email))
                .willReturn(Optional.of(account));

        // when
        authServiceImpl.login(email, password);
        // then
    }

    // ----- 회원가입 인증 메일 보내기 -----
    @DisplayName("회원가입 인증 메일 보내기 성공")
    @Test
    void testSendSignupVerificationEmailSuccess() {
        // given
        String email = "testEmail@gmail.com";

        // when
        authServiceImpl.sendSignupVerificationEmail(email);

        // then
        then(emailService).should().sendEmail(eq(email), any(), any());
    }

    @DisplayName("회원가입 인증 메일 보내기 실패 - 유효하지 않은 이메일")
    @Test
    void testSendSignupVerificationEmailFailureByInvalidEmail() {
        // given
        String email = "InvalidEmail";

        // when
        // then
        assertThrows(InvalidEmailException.class, () -> {
            authServiceImpl.sendSignupVerificationEmail(email);
        });
    }

    // ----- 이메일 인증 -----
    @DisplayName("이메일 인증 실패 - Redis 에 email Key 존재하지 않음")
    @Test
    void testVerifyEmailFailureByInvalidRedisKey() {
        // given
        String key = "InvalidKey";

        // when
        // then
        assertNull(redisUtil.getData(key));
    }

    @DisplayName("이메일 인증 실패 - 존재하지 않는 이메일")
    @Test
    void testVerifyEmailFailureByInvalidEmail() {
        // given
        String key = "eyJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50RW1haWwiOiJxdWVlbnN1YmluQG5hdmVyLmNvbSIsImlhdCI6MTYyMDcxMDc2OCwiZXhwIjoxNjIwNzIwODQ4fQ.V0RTILEB72nCraqRO_OhTIXzzlQ9CxHlkyheyfaVDVU";
        given(redisUtil.getData(key)).willReturn("queensubin@naver.com");

        // when
        // then
        assertThrows(AccountNotFoundException.class,
                () -> authServiceImpl.verifyEmail(key));
        then(redisUtil).should().getData(key);
        then(accountRepository).should().findByEmail("queensubin@naver.com");
    }

    @DisplayName("이메일 인증 성공")
    @Test
    void testVerifyEmailSuccess() {
        // given
        String key = "eyJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50RW1haWwiOiJraW5nc3ViaW5AbmF2ZXIuY29tIiwiaWF0IjoxNjIwMTI3NjA1LCJleHAiOjE2MjAxMzA0ODV9.zOiWpqoVX3R_EjeV1DL1lQwlZFnSec4RrFRQuK-IZV4";
        given(redisUtil.getData(key)).willReturn("kingsubin@naver.com");

        Account account = createAccount();
        given(accountRepository.findByEmail("kingsubin@naver.com"))
                .willReturn(Optional.of(account));

        // when
        authServiceImpl.verifyEmail(key);

        // then
        assertEquals(Role.LEVEL_1, account.getRole());
        then(redisUtil).should().deleteData(key);
    }

    // ----- 임시 패스워드 전송 -----
    @DisplayName("임시패스워드 전송 실패 - 존재하지 않는 이메일")
    @Test
    void testSendTempPasswordEmailFailureByInvalidEmail() {
        // given
        String email = "InvalidEmail";

        // when
        // then
        assertThrows(AccountNotFoundException.class, () -> {
            authServiceImpl.sendTempPasswordEmail(email);
        });
    }

    @DisplayName("임시패스워드 전송 성공 - 패스워드 재설정 후 이메일 전송")
    @Test
    void testSendTempPasswordEmailSuccess() {
        // given
        String email = "test@test.com";
        Account account = createAccount();
        given(accountRepository.findByEmail(email)).willReturn(Optional.of(account));

        MockedStatic<UUID> mockUUID = mockStatic(UUID.class);
        mockUUID.when(UUID::randomUUID).thenReturn(defaultUUID);

        // when
        authServiceImpl.sendTempPasswordEmail(email);

        // then
        then(emailService).should().sendEmail(eq(email), any(), any());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());
        assertEquals(
                accountArgumentCaptor.getValue().getPassword(),
                saltService.encodePassword(accountArgumentCaptor.getValue().getSalt(), defaultUUID.toString())
        );
    }

    // ----- 패스워드 업데이트 -----
    @DisplayName("패스워드 업데이트 실패 - 패스워드 불일치")
    @Test
    void testUpdatePasswordFailureByNotMatchedPassword() {
        // given
        AccountPasswordUpdateRequestDto passwordUpdateRequestDto = createPasswordUpdateRequestDto();
        passwordUpdateRequestDto.setCurrentPassword("InvalidPassword");
        Account account = createAccount();

        // when
        // then
        assertThrows(AccountPasswordNotMatchedException.class, () -> {
            authServiceImpl.updatePassword(
                    account,
                    passwordUpdateRequestDto.getCurrentPassword(),
                    passwordUpdateRequestDto.getNewPassword(),
                    passwordUpdateRequestDto.getConfirmNewPassword()
            );
        });
    }

    @DisplayName("패스워드 업데이트 실패 - 패스워드 정규식")
    @Test
    void testUpdatePasswordFailureByInvalidPassword() {
        // given
        AccountPasswordUpdateRequestDto passwordUpdateRequestDto = createPasswordUpdateRequestDto();
        passwordUpdateRequestDto.setNewPassword("InvalidPass");
        passwordUpdateRequestDto.setConfirmNewPassword("InvalidPass");
        Account account = createAccount();

        // when
        // then
        assertThrows(InvalidPasswordException.class, () -> {
            authServiceImpl.updatePassword(
                    account,
                    passwordUpdateRequestDto.getCurrentPassword(),
                    passwordUpdateRequestDto.getNewPassword(),
                    passwordUpdateRequestDto.getConfirmNewPassword()
            );
        });
    }

    @DisplayName("패스워드 업데이트 실패 - 패스워드, 확인패스워드 불일치")
    @Test
    void testUpdatePasswordFailureByNotEqualPassword() {
        // given
        AccountPasswordUpdateRequestDto passwordUpdateRequestDto = createPasswordUpdateRequestDto();
        passwordUpdateRequestDto.setNewPassword("newPassword123!");
        passwordUpdateRequestDto.setConfirmNewPassword("differentNewPassword123!");
        Account account = createAccount();

        // when
        // then
        assertThrows(AccountPasswordNotMatchedException.class, () -> {
            authServiceImpl.updatePassword(
                    account,
                    passwordUpdateRequestDto.getCurrentPassword(),
                    passwordUpdateRequestDto.getNewPassword(),
                    passwordUpdateRequestDto.getConfirmNewPassword()
            );
        });
    }

    @DisplayName("패스워드 업데이트 성공 - 패스워드 일치")
    @Test
    void testUpdatePasswordSuccess() {
        // given
        AccountPasswordUpdateRequestDto passwordUpdateRequestDto = createPasswordUpdateRequestDto();
        Account account = createAccount();

        // when
        authServiceImpl.updatePassword(
                account,
                passwordUpdateRequestDto.getCurrentPassword(),
                passwordUpdateRequestDto.getNewPassword(),
                passwordUpdateRequestDto.getConfirmNewPassword()
        );

        // then
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());
        assertEquals(
                accountArgumentCaptor.getValue().getPassword(),
                saltService.encodePassword(accountArgumentCaptor.getValue().getSalt(), passwordUpdateRequestDto.getNewPassword())
        );
    }
}
