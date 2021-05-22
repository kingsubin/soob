package com.community.soob.account.controller;

import com.community.soob.account.config.JwtRequestFilter;
import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.Role;
import com.community.soob.account.service.AccountService;
import com.community.soob.account.service.AuthService;
import com.community.soob.account.service.CustomUserDetailsService;
import com.community.soob.account.service.SaltService;
import com.community.soob.account.service.validator.NicknameUpdateValidator;
import com.community.soob.account.service.validator.PasswordUpdateValidator;
import com.community.soob.account.service.validator.SignupValidator;
import com.community.soob.attachment.AttachmentService;
import com.community.soob.util.CookieUtil;
import com.community.soob.util.JwtUtil;
import com.community.soob.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired CookieUtil cookieUtil;

    @Autowired SaltService saltService;

    @MockBean AuthService authService;
    @MockBean AccountService accountService;

    @MockBean AccountRepository accountRepository;
    @MockBean AttachmentService attachmentService;
    @MockBean RedisUtil redisUtil;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtRequestFilter jwtRequestFilter;

    @MockBean NicknameUpdateValidator nicknameUpdateValidator;
    @MockBean PasswordUpdateValidator passwordUpdateValidator;
    @MockBean SignupValidator signupValidator;

    @InitBinder("signupRequestDto")
    void signupInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signupValidator);
    }

    @InitBinder("nicknameUpdateRequestDto")
    void nicknameInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameUpdateValidator);
    }

    @InitBinder("passwordUpdateRequestDto")
    void passwordInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordUpdateValidator);
    }

    @DisplayName("닉네임 유효성 검사 성공")
    @Test
    void nicknameTest1() {
    }

    @DisplayName("닉네임 유효성 검사 실패")
    @Test
    void nicknameTest2() {

    }

    @DisplayName("비밀번호 유효성 검사 성공")
    @Test
    void passwordTest1() {

    }

    @DisplayName("비밀번호 유효성 검사 실패")
    @Test
    void passwordTest2() {

    }

    @DisplayName("회원가입 유효성 검사 성공")
    @Test
    void signupTest1() {

    }

    @DisplayName("회원가입 유효성 검사 실패")
    @Test
    void signupTest2() {

    }

    @DisplayName("회원가입성공")
    @Test
    void successSignup() {
//        // given
//        AccountSignupRequestDto signupRequestDto = new AccountSignupRequestDto();
//        signupRequestDto.setEmail("test@naver.com");
//        signupRequestDto.setNickname("test");
//        signupRequestDto.setPassword("password");
//        signupRequestDto.setConfirmPassword("password");
//        signupRequestDto.setSalt(saltService.genSalt());
//
//        // when
//        ResultActions actions = mockMvc.perform(post("/api/v1/accounts")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .characterEncoding("UTF-8"));

        // then
    }

    @Test
    void signupAccount() {
    }

    @Test
    void login() throws Exception {
        AccountLoginRequestDto loginRequestDto = new AccountLoginRequestDto();
        String email = "test@test.com";
        loginRequestDto.setEmail(email);
        loginRequestDto.setPassword("password");

        Account account = Account.builder()
                .id(1L)
                .email(email)
                .password("$2a$10$2H.qwzvH9zq4NrqrGJWdZOVZ4nrx3rfgEqnKvK98fWvaop0ceVtt2")
                .nickname("킹수빈")
                .role(Role.LEVEL_1)
                .salt("$2a$10$2H.qwzvH9zq4NrqrGJWdZO")
                .profileImage(null)
                .build();

        when(authService.login(loginRequestDto))
                .thenReturn(account);

        verify(jwtUtil).generateToken(email);
        verify(jwtUtil).generateRefreshToken(email);

        verify(cookieUtil, times(2)).createCookie(any(), any());

        verify(redisUtil).setDataExpire(any(), any(), any());

        mockMvc.perform(post("/api/v1/accounts"))
                .andDo(print())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(status().isOk());
    }

    @Test
    void logout() {
    }

    @Test
    void retrieveAccount() {
    }

    @Test
    void updateAccount() {
    }

    @Test
    void deleteAccount() {
    }

    @Test
    void updatePassword() {
    }

    @Test
    void checkEmailDuplicated() {
    }

    @Test
    void checkNicknameDuplicated() {
    }

    @Test
    void verifyEmail() {
    }

    @Test
    void sendSignupEmail() {
    }

    @Test
    void sendPasswordEmail() {
    }
}
