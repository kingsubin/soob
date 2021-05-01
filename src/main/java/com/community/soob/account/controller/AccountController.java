package com.community.soob.account.controller;

import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.controller.dto.AccountResponseDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.service.AccountService;
import com.community.soob.account.service.AuthService;
import com.community.soob.response.ResultResponse;
import com.community.soob.util.CookieUtil;
import com.community.soob.util.JwtUtil;
import com.community.soob.util.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@RestController
public class AccountController {
    private final AuthService authService;
    private final AccountService accountService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisUtil redisUtil;

    @Operation(summary = "회원가입", description = "회원가입을 한다.")
    @PostMapping
    public ResultResponse<Void> signupAccount(
            @Parameter(name = "회원가입DTO", required = true) @Valid @RequestBody final AccountSignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @Operation(summary = "회원 로그인", description = "email 과 password 로 로그인한다.")
    @PostMapping("/login")
    public ResultResponse<Void> login(
            @Parameter(name = "로그인DTO", required = true) @Valid @RequestBody final AccountLoginRequestDto loginRequestDto,
                                      HttpServletResponse response) {
        Account account = authService.login(loginRequestDto);
        String accountEmail = account.getEmail();

        String jwt = jwtUtil.generateToken(accountEmail);
        String refreshJwt = jwtUtil.generateRefreshToken(accountEmail);
        Cookie accessToken = cookieUtil.createCookie(JwtUtil.ACCESS_TOKEN_NAME, jwt);
        Cookie refreshToken = cookieUtil.createCookie(JwtUtil.REFRESH_TOKEN_NAME, refreshJwt);

        redisUtil.setDataExpire(refreshJwt, accountEmail, JwtUtil.REFRESH_TOKEN_VALIDATION_SECOND);

        // 응답 객체에 쿠키를 저장
        response.addCookie(accessToken);
        response.addCookie(refreshToken);

        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    // 로그아웃 미구현
    // /accounts/logout
    @Operation(summary = "회원 로그아웃", description = "로그아웃한다.")
    @GetMapping("/logout")
    public ResultResponse<Void> logout() {
        authService.logout();
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @Operation(summary = "회원 단건 조회", description = "회원번호로 회원을 조회한다.")
    @GetMapping("/{accountId}")
    public ResultResponse<AccountResponseDto> retrieveAccount(
            @Parameter(name = "회원번호", required = true) @PathVariable Long accountId) {
        AccountResponseDto accountResponseDto = AccountResponseDto.fromEntity(accountService.findById(accountId));
        return ResultResponse.of(ResultResponse.SUCCESS, accountResponseDto);
    }

    @Operation(summary = "회원 삭제", description = "회원번호(id) 로 회원정보를 삭제한다.")
    @DeleteMapping("/{accountId}")
    public ResultResponse<Void> deleteAccount(
            @Parameter(name = "회원번호", required = true) @PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @Operation(summary = "이메일 중복체크", description = "이메일 중복체크를 한다.")
    @GetMapping("/check-email/{email}")
    public ResultResponse<Boolean> checkEmailDuplicated(
            @Parameter(name = "EMAIL", required = true) @PathVariable String email) {
        return ResultResponse.of(ResultResponse.SUCCESS, accountService.checkEmailDuplicated(email));
    }

    @Operation(summary = "닉네임 중복체크", description = "닉네임 중복체크를 한다.")
    @GetMapping("/check-nickname/{nickname}")
    public ResultResponse<Boolean> checkNicknameDuplicated(
            @Parameter(name = "NICKNAME", required = true) @PathVariable String nickname) {
        return ResultResponse.of(ResultResponse.SUCCESS, accountService.checkNicknameDuplicated(nickname));
    }

    @Operation(summary = "이메일 인증 체크", description = "발급받은 key 값으로 인증 유효성 체크를 한다.")
    @GetMapping("/verify/{key}")
    public ResultResponse<Void> verifyEmail(
            @Parameter(name = "KEY", required = true) @PathVariable String key) {
        authService.verifyEmail(key);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @Operation(summary = "회원가입 이메일 전송", description = "작성한 이메일로 회원가입 인증 이메일을 보낸다.")
    @GetMapping("/send-signup-email/{email}")
    public ResultResponse<Void> sendSignupEmail(
            @Parameter(name = "EMAIL", required = true) @PathVariable String email) {
        authService.sendSignupVerificationEmail(email);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @Operation(summary = "비밀번호 찾기 이메일 전송", description = "작성한 이메일로 임시비밀번호를 담은 이메일 보낸다.")
    @GetMapping("/send-temp-password/{email}")
    public ResultResponse<Void> sendPasswordEmail(
            @Parameter(name = "EMAIL", required = true) @PathVariable String email) {
        authService.sendTempPasswordEmail(email);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }
}

