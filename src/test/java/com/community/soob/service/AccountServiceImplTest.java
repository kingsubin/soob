package com.community.soob.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.exception.InvalidNicknameException;
import com.community.soob.account.service.AccountServiceImpl;
import com.community.soob.account.service.AuthService;
import com.community.soob.attachment.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    private AccountServiceImpl accountServiceImpl;

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

    @BeforeEach
    void setUp() {
        this.accountServiceImpl = new AccountServiceImpl(
                accountRepository, attachmentService, authService
        );
    }

    @DisplayName("회원 변경 성공 - 파일 없음")
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

    // 실제로 이미지가 들어가진 않음 ,,
    @DisplayName("회원 변경 성공 - 파일 있음")
    @Test
    void testUpdateAccountSuccessWithFile() throws IOException {
        // given
        String nickname = "modifiedNickname";
        Account account = createAccount();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                "image/jpeg",
                "image".getBytes()
        );

        // when
        accountServiceImpl.updateAccount(account, nickname, multipartFile);

        // then
        assertEquals(nickname, account.getNickname());
        then(attachmentService).should().uploadProfileImage(eq(account), eq(multipartFile), any());
        then(accountRepository).should().save(account);
    }

    @DisplayName("회원 변경 실패 - 닉네임 정규식")
    @Test
    void testUpdateAccountFailureByInvalidNickname() {
        // given
        String nickname = "InvalidNickname!@#";
        Account account = createAccount();

        // when
        // then
        assertThrows(InvalidNicknameException.class, () -> {
            accountServiceImpl.updateAccount(account, nickname, null);
        });
    }

    @DisplayName("회원 변경 실패 - 파일 관련문제")
    @Test
    void testUpdateAccountFailureByFile() throws IOException {
        // given
        String nickname = "modifiedNickname";
        Account account = createAccount();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                "image/jpeg",
                "image".getBytes()
        );

        // when

        // then
    }

    /*
    ArgumentCaptor 예제
    @DisplayName("가입하면 메일을 전송함")
    @Test
    void whenRegisterThenSendMail() {
        userRegister.register("id", "pw", "email@email.com");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        BDDMockito.then(mockEmailNotifier).should().sendRegisterEmail(captor.capture());

        String realEmail = captor.getValue();
        assertEquals("email@email.com", realEmail);
    }
     */
}
