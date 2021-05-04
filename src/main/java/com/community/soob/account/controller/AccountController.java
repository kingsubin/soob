package com.community.soob.account.controller;

import com.community.soob.account.config.CurrentAccount;
import com.community.soob.account.controller.dto.*;
import com.community.soob.account.domain.Account;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import com.community.soob.account.service.AccountService;
import com.community.soob.account.service.AuthService;
import com.community.soob.account.service.SaltService;
import com.community.soob.account.service.validator.NicknameUpdateValidator;
import com.community.soob.account.service.validator.PasswordUpdateValidator;
import com.community.soob.account.service.validator.SignupValidator;
import com.community.soob.attachment.Attachment;
import com.community.soob.attachment.AttachmentException;
import com.community.soob.attachment.AttachmentInfo;
import com.community.soob.attachment.AttachmentService;
import com.community.soob.response.ResultResponse;
import com.community.soob.util.CookieUtil;
import com.community.soob.util.JwtUtil;
import com.community.soob.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@Api(tags = {"1, Account"})
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@RestController
public class AccountController {
    private final AuthService authService;
    private final AccountService accountService;
    private final AttachmentService attachmentService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisUtil redisUtil;
    private final SaltService saltService;

    private final NicknameUpdateValidator nicknameUpdateValidator;
    private final PasswordUpdateValidator passwordUpdateValidator;
    private final SignupValidator signupValidator;

    @InitBinder("signupRequestDto")
    public void signupInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signupValidator);
    }

    @InitBinder("nicknameUpdateRequestDto")
    public void nicknameInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameUpdateValidator);
    }

    @InitBinder("passwordUpdateRequestDto")
    public void passwordInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordUpdateValidator);
    }

    @ApiOperation(value = "회원가입", notes = "회원가입을 한다.")
    @PostMapping
    public ResultResponse<Void> signupAccount(
            @ApiParam(value = "회원가입DTO", required = true) @Valid @RequestBody final AccountSignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원 로그인", notes = "email 과 password 로 로그인한다.")
    @PostMapping("/login")
    public ResultResponse<Void> login(
            @ApiParam(value = "로그인DTO", required = true) @Valid @RequestBody final AccountLoginRequestDto loginRequestDto,
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
    @ApiOperation(value = "회원 로그아웃", notes = "로그아웃한다.")
    @GetMapping("/logout")
    public ResultResponse<Void> logout() {
        authService.logout();
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원 단건 조회", notes = "회원번호로 회원을 조회한다.")
    @GetMapping("/{accountId}")
    public ResultResponse<AccountResponseDto> retrieveAccount(
            @ApiParam(value = "회원번호", required = true) @PathVariable Long accountId) {
        AccountResponseDto accountResponseDto = AccountResponseDto.fromEntity(accountService.findById(accountId));
        return ResultResponse.of(ResultResponse.SUCCESS, accountResponseDto);
    }

    @ApiOperation(value = "회원 프로필 정보 수정", notes = "회원 프로필 정보를 수정한다.")
    @PutMapping("/{accountId}")
    public ResultResponse<AccountResponseDto> updateAccount(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "회원번호", required = true) @PathVariable Long accountId,
            @ApiParam(value = "닉네임DTO")  @Valid @RequestBody final AccountNicknameUpdateRequestDto nicknameUpdateRequestDto,
            @ApiParam(value = "프로필이미지") @RequestParam("profileImage") MultipartFile file) {
        // 프로필이미지 존재시
        if (file != null && !file.isEmpty()) {
            AttachmentInfo attachmentInfo = null;
            try {
                attachmentInfo = new AttachmentInfo(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes()
                );
            } catch (IOException e) {
                throw new AttachmentException();
            }

            Attachment attachment = attachmentService.saveProfileImage(attachmentInfo);
            accountService.updateProfileImage(accountId, attachment);
        }

        // 프로필이미지가 없을시
        accountService.updateNickname(accountId, nicknameUpdateRequestDto.getNickname());

        AccountResponseDto accountResponseDto = AccountResponseDto.fromEntity(accountService.findById(accountId));
        return ResultResponse.of(ResultResponse.SUCCESS, accountResponseDto);
    }

    @ApiOperation(value = "회원 삭제", notes = "회원번호(id) 로 회원정보를 삭제한다.")
    @DeleteMapping("/{accountId}")
    public ResultResponse<Void> deleteAccount(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "회원번호", required = true) @PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원 비밀번호 변경", notes = "회원 비밀번호를 변경한다.")
    @PatchMapping("/{accountId}")
    public ResultResponse<Void> updatePassword(
            @ApiIgnore(value = "로그인한 유저인지 검사") @CurrentAccount Account account,
            @ApiParam(value = "회원번호", required = true) @PathVariable Long accountId,
            @ApiParam(value = "비밀번호변경DTO", required = true) @RequestBody @Valid final AccountPasswordUpdateRequestDto passwordUpdateRequestDto) {
        boolean matches = saltService.matches(passwordUpdateRequestDto.getCurrentPassword(), account.getPassword());
        if (!matches) {
            throw new AccountPasswordNotMatchedException();
        }

        authService.updatePassword(accountId, passwordUpdateRequestDto.getNewPassword());
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "이메일 중복체크", notes = "이메일 중복체크를 한다.")
    @GetMapping("/check-email/{email}")
    public ResultResponse<Boolean> checkEmailDuplicated(
            @ApiParam(value = "EMAIL", required = true) @PathVariable String email) {
        return ResultResponse.of(ResultResponse.SUCCESS, accountService.checkEmailDuplicated(email));
    }

    @ApiOperation(value = "닉네임 중복체크", notes = "닉네임 중복체크를 한다.")
    @GetMapping("/check-nickname/{nickname}")
    public ResultResponse<Boolean> checkNicknameDuplicated(
            @ApiParam(value = "NICKNAME", required = true) @PathVariable String nickname) {
        return ResultResponse.of(ResultResponse.SUCCESS, accountService.checkNicknameDuplicated(nickname));
    }

    @ApiOperation(value = "이메일 인증 체크", notes = "발급받은 key 값으로 인증 유효성 체크를 한다.")
    @GetMapping("/verify/{key}")
    public ResultResponse<Void> verifyEmail(
            @ApiParam(value = "KEY", required = true) @PathVariable String key) {
        authService.verifyEmail(key);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "회원가입 이메일 전송", notes = "작성한 이메일로 회원가입 인증 이메일을 보낸다.")
    @GetMapping("/send-signup-email/{email}")
    public ResultResponse<Void> sendSignupEmail(
            @ApiParam(value = "EMAIL", required = true) @PathVariable String email) {
        authService.sendSignupVerificationEmail(email);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }

    @ApiOperation(value = "비밀번호 찾기 이메일 전송", notes = "작성한 이메일로 임시비밀번호를 담은 이메일 보낸다.")
    @GetMapping("/send-temp-password/{email}")
    public ResultResponse<Void> sendPasswordEmail(
            @ApiParam(value = "EMAIL", required = true) @PathVariable String email) {
        authService.sendTempPasswordEmail(email);
        return ResultResponse.of(ResultResponse.SUCCESS);
    }
}

