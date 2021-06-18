package com.community.soob.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import com.community.soob.account.service.*;
import com.community.soob.attachment.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountUpdateServiceTest {
    @InjectMocks AccountUpdateService accountUpdateService;
    @Mock private AccountRepository accountRepository;
    @Mock private EmailService emailService;
    @Mock private AccountFindService accountFindService;
    @Mock private AccountCheckService accountCheckService;
    @Mock private AttachmentService attachmentService;
    private SaltService saltService = new SaltService();

    private Account createAccount() {
        return Account.builder()
                .id(2L)
                .email("test@test.com")
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("test")
                .levelPoint(50)
                .role(Role.LEVEL_1)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();
    }

    @BeforeEach
    void setUp() {
        this.accountUpdateService = new AccountUpdateService(
                accountRepository, emailService, accountFindService, accountCheckService, attachmentService, saltService
        );
    }

    @DisplayName("패스워드 업데이트 성공")
    @Test
    void testUpdatePasswordSuccess() {
        // given
        Account account = createAccount();
        String currentPassword = "password";
        String newPassword = "Password123!@#";
        String confirmNewPassword = "Password123!@#";

        // when
        accountUpdateService.updatePassword(account, currentPassword, newPassword, confirmNewPassword);

        // then
        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());

        assertEquals(
                accountArgumentCaptor.getValue().getPassword(),
                saltService.encodePassword(accountArgumentCaptor.getValue().getSalt(), newPassword)
        );
    }

    @DisplayName("패스워드 업데이트 실패 - 패스워드 불일치")
    @Test
    void testUpdatePasswordFailureByNotMatchedPassword() {
        // given
        Account account = createAccount();
        String currentPassword = "InvalidPassword";
        String newPassword = "Password123!@#";
        String confirmNewPassword = "Password123!@#";

        // when
        // then
        assertThrows(AccountPasswordNotMatchedException.class,
                () -> accountUpdateService.updatePassword(account, currentPassword, newPassword, confirmNewPassword)
        );
    }

    @DisplayName("임시패스워드전송 성공")
    @Test
    void testSendTempPasswordEmailSuccess() {
        // given
        String email = "test@test.com";
        Account account = createAccount();
        given(accountFindService.findByEmail(email))
                .willReturn(account);

        final UUID tempPassword = UUID.fromString("d49de159-7c60-41bb-9f4c-51ba1087f696");
        MockedStatic<UUID> mockUUID = mockStatic(UUID.class);
        mockUUID.when(UUID::randomUUID).thenReturn(tempPassword);

        // when
        accountUpdateService.sendTempPasswordEmail(email);

        // then
        then(emailService).should().sendEmail(eq(email), any(), any());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(accountArgumentCaptor.capture());
        assertEquals(
                accountArgumentCaptor.getValue().getPassword(),
                saltService.encodePassword(accountArgumentCaptor.getValue().getSalt(), tempPassword.toString())
        );
    }

    @DisplayName("회원 정보 수정 성공 - 이미지 존재")
    @Test
    void testUpdateAccountSuccessWithProfileImage() throws IOException {
        // given
        Account account = createAccount();
        String nickname = "modifiedNickname";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                "image/jpeg",
                new ClassPathResource("/images/profileImage.jpg").getInputStream()
        );

        // when
        accountUpdateService.updateAccount(account, nickname, multipartFile);

        // then
        assertEquals(nickname, account.getNickname());
        then(attachmentService).should().uploadProfileImage(eq(account), eq(multipartFile));
        then(accountRepository).should().save(account);
    }

    @DisplayName("회원 정보 수정 성공 - 이미지 없음")
    @Test
    void testUpdateAccountSuccessWithoutProfileImage() {
        // given
        String nickname = "modifiedNickname";
        Account account = createAccount();

        // when
        accountUpdateService.updateAccount(account, nickname, null);

        // then
        assertEquals(nickname, account.getNickname());
        then(attachmentService).should(never()).uploadProfileImage(any(), any());
        then(accountRepository).should().save(account);
    }
}
