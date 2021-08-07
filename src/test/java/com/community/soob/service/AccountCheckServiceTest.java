package com.community.soob.service;

import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import com.community.soob.account.exception.InvalidEmailException;
import com.community.soob.account.exception.InvalidNicknameException;
import com.community.soob.account.exception.InvalidPasswordException;
import com.community.soob.account.service.AccountCheckService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountCheckServiceTest {
    @InjectMocks private AccountCheckService accountCheckService;
    @Mock private AccountRepository accountRepository;

    @DisplayName("이메일 중복검사 성공")
    @Test
    void testCheckEmailDuplicatedReturnSuccess() {
        // given
        String email = "kingsubin@naver.com";
        given(accountRepository.existsByEmail(email)).willReturn(false);

        // when
        // then
        assertFalse(accountCheckService.checkEmailDuplicated(email));
    }

    @DisplayName("이메일 중복검사 실패 - 중복 이메일 존재")
    @Test
    void testCheckEmailDuplicatedFailureByDuplicatedEmail() {
        // given
        String email = "kingsubin@naver.com";
        given(accountRepository.existsByEmail(email)).willReturn(true);

        // when
        // then
        assertTrue(accountCheckService.checkEmailDuplicated(email));
    }

    @DisplayName("닉네임 중복검사 성공")
    @Test
    void testCheckNicknameDuplicatedSuccess() {
        // given
        String nickname = "kingsubin";
        given(accountRepository.existsByNickname(nickname)).willReturn(false);

        // when
        // then
        assertFalse(accountCheckService.checkNicknameDuplicated(nickname));
    }

    @DisplayName("닉네임 중복검사 실패 - 중복된 닉네임 존재")
    @Test
    void testCheckNicknameDuplicatedFailureByDuplicatedNickname() {
        // given
        String nickname = "kingsubin";
        given(accountRepository.existsByNickname(nickname)).willReturn(true);

        // when
        // then
        assertTrue(accountCheckService.checkNicknameDuplicated(nickname));
    }

    @DisplayName("이메일 정규 표현식 검사")
    @Test
    void testCheckEmailRegex() {
        // given
        String successEmail1 = "kingsubin9492@google.com";
        String successEmail2 = "kingsubin@naver.com";
        String failEmail1 = "kingsubin";
        String failEmail2 = "kingsubin!@@a.z";
        String failEmail3 = "king!!!!@naver.com";

        // when
        // then
        assertAll(
                () -> assertDoesNotThrow(() -> accountCheckService.checkEmailRegex(successEmail1)),
                () -> assertDoesNotThrow(() -> accountCheckService.checkEmailRegex(successEmail2)),
                () -> assertThrows(InvalidEmailException.class, () -> accountCheckService.checkEmailRegex(failEmail1)),
                () -> assertThrows(InvalidEmailException.class, () -> accountCheckService.checkEmailRegex(failEmail2)),
                () -> assertThrows(InvalidEmailException.class, () -> accountCheckService.checkEmailRegex(failEmail3))
        );
    }

    @DisplayName("닉네임 정규 표현식 검사")
    @Test
    void testCheckNicknameRegex() {
        // given
        String successNickname1 = "kingsubin";
        String successNickname2 = "KINGsubin01243";
        String failNickname1 = "kingsubin!";
        String failNickname2 = "Kingusubin!23";
        String failNickname3 = "king!!!!@naver.com";

        // when
        // then
        assertAll(
                () -> assertDoesNotThrow(() -> accountCheckService.checkNicknameRegex(successNickname1)),
                () -> assertDoesNotThrow(() -> accountCheckService.checkNicknameRegex(successNickname2)),
                () -> assertThrows(InvalidNicknameException.class, () -> accountCheckService.checkNicknameRegex(failNickname1)),
                () -> assertThrows(InvalidNicknameException.class, () -> accountCheckService.checkNicknameRegex(failNickname2)),
                () -> assertThrows(InvalidNicknameException.class, () -> accountCheckService.checkNicknameRegex(failNickname3))
        );
    }

    @DisplayName("패스워드 정규 표현식 검사")
    @Test
    void testCheckPasswordRegex() {
        // given
        String successPassword1 = "01234567891011";
        String successPassword2 = "kingsubin!@#$%";
        String successPassword3 = "KINGsubin00000000";
        String successPassword4 = "KINGSUBIN..==()()";
        String successPassword5 = "kingsubinkingsubinkingsubin";
        String failPassword1 = "kingsubin";
        String failPassword2 = "kingsubin''''''''";
        String failPassword3 = "KINGsubin,,,,,,,,";
        String failPassword4 = "KINGSUBIN????????";

        // when
        // then
        assertAll(
                () -> assertDoesNotThrow(() -> accountCheckService.checkPasswordRegex(successPassword1)),
                () -> assertDoesNotThrow(() -> accountCheckService.checkPasswordRegex(successPassword2)),
                () -> assertDoesNotThrow(() -> accountCheckService.checkPasswordRegex(successPassword3)),
                () -> assertDoesNotThrow(() -> accountCheckService.checkPasswordRegex(successPassword4)),
                () -> assertDoesNotThrow(() -> accountCheckService.checkPasswordRegex(successPassword5)),
                () -> assertThrows(InvalidPasswordException.class, () -> accountCheckService.checkPasswordRegex(failPassword1)),
                () -> assertThrows(InvalidPasswordException.class, () -> accountCheckService.checkPasswordRegex(failPassword2)),
                () -> assertThrows(InvalidPasswordException.class, () -> accountCheckService.checkPasswordRegex(failPassword3)),
                () -> assertThrows(InvalidPasswordException.class, () -> accountCheckService.checkPasswordRegex(failPassword4))
        );
    }

    @DisplayName("패스워드 매칭 성공")
    @Test
    void testCheckPasswordMatchingSuccess() {
        // given
        String password = "KINGsubin123!@#";
        String confirmPassword = "KINGsubin123!@#";

        // when
        // then
        assertDoesNotThrow(() ->
                accountCheckService.checkPasswordMatching(password, confirmPassword));
    }

    @DisplayName("패스워드 매칭 실패 - 불일치")
    @Test
    void testCheckPasswordMatchingFailureByNotMatchedPassword() {
        // given
        String password = "KINGsubin123!@#";
        String confirmPassword = "InvalidPassword1!@";

        // when
        // then
        assertThrows(AccountPasswordNotMatchedException.class,
                () -> accountCheckService.checkPasswordMatching(password, confirmPassword));
    }
}
