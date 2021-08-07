package com.community.soob.account.controller;

import com.community.soob.account.config.CurrentAccount;
import com.community.soob.account.controller.dto.*;
import com.community.soob.account.domain.Account;
import com.community.soob.account.service.*;
import com.community.soob.response.ResultResponse;
import com.community.soob.util.CookieUtil;
import com.community.soob.util.JwtUtil;
import com.community.soob.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(tags = {"1, Account"})
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@RestController
public class AccountController {
    private final AccountSignupService accountSignupService;
    private final AccountLoginService accountLoginService;
    private final AccountFindService accountFindService;
    private final AccountUpdateService accountUpdateService;
    private final AccountCheckService accountCheckService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisUtil redisUtil;

    @ApiOperation(value = "회원가입", notes = "회원가입을 한다.")
    @PostMapping
    public ResultResponse<Void> signupAccount(
            @ApiParam(value = "회원가입DTO", required = true) @Valid @RequestBody final AccountSignupRequestDto signupRequestDto) {
        accountSignupService.signup(signupRequestDto);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원 로그인", notes = "email 과 password 로 로그인한다.")
    @PostMapping("/login")
    public ResultResponse<Void> login(
            @ApiParam(value = "로그인DTO", required = true) @Valid @RequestBody final AccountLoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        Account account = accountLoginService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        String accountEmail = account.getEmail();

        String jwt = jwtUtil.generateToken(accountEmail);
        String refreshJwt = jwtUtil.generateRefreshToken(accountEmail);
        Cookie accessToken = cookieUtil.createCookie(JwtUtil.ACCESS_TOKEN_NAME, jwt);
        Cookie refreshToken = cookieUtil.createCookie(JwtUtil.REFRESH_TOKEN_NAME, refreshJwt);

        redisUtil.setDataExpire(refreshJwt, accountEmail, JwtUtil.REFRESH_TOKEN_VALIDATION_SECOND);

        response.addCookie(accessToken);
        response.addCookie(refreshToken);

        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원 로그아웃", notes = "로그아웃한다.")
    @GetMapping("/logout")
    public ResultResponse<Void> logout(HttpServletResponse response) {
        Cookie accessToken = cookieUtil.createCookie(JwtUtil.ACCESS_TOKEN_NAME, null);
        Cookie refreshToken = cookieUtil.createCookie(JwtUtil.REFRESH_TOKEN_NAME, null);
        accessToken.setMaxAge(0);
        refreshToken.setMaxAge(0);
        response.addCookie(accessToken);
        response.addCookie(refreshToken);

        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원 단건 조회", notes = "회원번호로 회원을 조회한다.")
    @GetMapping("/{accountId}")
    public ResultResponse<AccountResponseDto> retrieveAccount(
            @ApiParam(value = "회원번호", required = true) @PathVariable Long accountId) {
        AccountResponseDto accountResponseDto = AccountResponseDto.fromEntity(accountFindService.findById(accountId));
        return ResultResponse.of(ResultResponse.SUCCESS, accountResponseDto);
    }

    @ApiOperation(value = "회원 프로필 정보 수정", notes = "회원 프로필 정보를 수정한다.")
    @PutMapping("/{accountId}")
    public ResultResponse<AccountResponseDto> updateAccount(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "회원번호", required = true) @PathVariable Long accountId,
            @ApiParam(value = "닉네임DTO")  @Valid @RequestBody final AccountNicknameUpdateRequestDto nicknameUpdateRequestDto,
            @ApiParam(value = "프로필이미지") @RequestPart(required = false, name = "file") MultipartFile file) {
        accountUpdateService.updateAccount(account, nicknameUpdateRequestDto.getNickname(), file);
        return ResultResponse.of(ResultResponse.SUCCESS, AccountResponseDto.fromEntity(account));
    }

    @ApiOperation(value = "회원 삭제", notes = "회원번호(id) 로 회원정보를 삭제한다.")
    @DeleteMapping("/{accountId}")
    public ResultResponse<Void> deleteAccount(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "회원번호", required = true) @PathVariable Long accountId) {
        accountUpdateService.deleteAccount(accountId);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원 비밀번호 변경", notes = "회원 비밀번호를 변경한다.")
    @PatchMapping("/password-update")
    public ResultResponse<Void> updatePassword(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "비밀번호변경DTO", required = true) @Valid @RequestBody final AccountPasswordUpdateRequestDto passwordUpdateRequestDto) {
        accountUpdateService.updatePassword(account, passwordUpdateRequestDto.getCurrentPassword(), passwordUpdateRequestDto.getNewPassword(), passwordUpdateRequestDto.getConfirmNewPassword());
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "이메일 중복체크", notes = "이메일 중복체크를 한다.")
    @GetMapping("/check-email/{email}")
    public ResultResponse<Boolean> checkEmailDuplicated(
            @ApiParam(value = "EMAIL", required = true) @PathVariable String email) {
        return ResultResponse.of(ResultResponse.SUCCESS, accountCheckService.checkEmailDuplicated(email));
    }

    @ApiOperation(value = "닉네임 중복체크", notes = "닉네임 중복체크를 한다.")
    @GetMapping("/check-nickname/{nickname}")
    public ResultResponse<Boolean> checkNicknameDuplicated(
            @ApiParam(value = "NICKNAME", required = true) @PathVariable String nickname) {
        return ResultResponse.of(ResultResponse.SUCCESS, accountCheckService.checkNicknameDuplicated(nickname));
    }

    @ApiOperation(value = "이메일 인증 체크", notes = "발급받은 key 값으로 인증 유효성 체크를 한다.")
    @GetMapping("/verify/{key}")
    public ResultResponse<Void> verifyEmail(
            @ApiParam(value = "KEY", required = true) @PathVariable String key) {
        accountSignupService.verifyEmail(key);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원가입 이메일 전송", notes = "작성한 이메일로 회원가입 인증 이메일을 보낸다.")
    @GetMapping("/send-signup-email/{email}")
    public ResultResponse<Void> sendSignupEmail(
            @ApiParam(value = "EMAIL", required = true) @PathVariable String email) {
        accountSignupService.sendSignupVerificationEmail(email);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "비밀번호 찾기 이메일 전송", notes = "작성한 이메일로 임시비밀번호를 담은 이메일 보낸다.")
    @GetMapping("/send-temp-password/{email}")
    public ResultResponse<Void> sendPasswordEmail(
            @ApiParam(value = "EMAIL", required = true) @PathVariable String email) {
        accountUpdateService.sendTempPasswordEmail(email);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }
}

