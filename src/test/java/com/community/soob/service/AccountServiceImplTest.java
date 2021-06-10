package com.community.soob.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.service.AccountServiceImpl;
import com.community.soob.account.service.AuthService;
import com.community.soob.attachment.AttachmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @InjectMocks private AccountServiceImpl accountServiceImpl;
    @Mock private AccountRepository accountRepository;
    @Mock private AttachmentService attachmentService;
    @Mock private AuthService authService;

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

    @DisplayName("회원 변경 성공 - 파일 null")
    @Test
    void testUpdateAccountSuccessWithoutFile() {
        // given
        String nickname = "modifiedNickname";
        Account account = createAccount();

        // when
        accountServiceImpl.updateAccount(account, nickname, null);

        // then
        assertEquals(nickname, account.getNickname());

        then(attachmentService).should(never()).uploadProfileImage(any(), any(), any());
        then(accountRepository).should(only()).save(account);
    }

    @DisplayName("회원 변경 성공 - 파일 not null")
    @Test
    void testUpdateAccountSuccessWithFile() throws IOException {
        // given
        String nickname = "modifiedNickname";
        Account account = createAccount();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                "image/jpeg",
                new ClassPathResource("/images/profileImage.jpg").getInputStream()
        );

        // when
        accountServiceImpl.updateAccount(account, nickname, multipartFile);

        // then
        assertEquals(nickname, account.getNickname());
        then(attachmentService).should().uploadProfileImage(eq(account), eq(multipartFile), any());
        then(accountRepository).should().save(account);
    }

    /*
    ArgumentCaptor 예제

    // 1
    @DisplayName("가입하면 메일을 전송함")
    @Test
    void whenRegisterThenSendMail() {
        userRegister.register("id", "pw", "email@email.com");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        BDDMockito.then(mockEmailNotifier).should().sendRegisterEmail(captor.capture());

        String realEmail = captor.getValue();
        assertEquals("email@email.com", realEmail);
    }

    // 2
    ArgumentCaptor<String> mailBodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendEmail(eq(email), any, mailBodyCaptor.capture());
    String[] tokens = mailBodyCaptor.getValue().split(" ");
    String passwordSent = tokens[tokens.length - 1];

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).save(accountCaptor.capture());

    assertTrue(saltService.matches(passwordSent, accountCaptor.getValue().getPassword()));
     */
}
