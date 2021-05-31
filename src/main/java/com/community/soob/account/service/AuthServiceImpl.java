package com.community.soob.account.service;

import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.controller.dto.AccountPasswordUpdateRequestDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.*;
import com.community.soob.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional(readOnly = true)
@Service
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final SaltService saltService;
    private final RedisUtil redisUtil;
    private String verificationDuration;
    private String verificationLink;

    public AuthServiceImpl(AccountRepository accountRepository, EmailService emailService, SaltService saltService, RedisUtil redisUtil, @Value("{email.verification.duration}") String verificationDuration,  @Value("{email.verification.link}") String verificationLink) {
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.saltService = saltService;
        this.redisUtil = redisUtil;
        this.verificationDuration = verificationDuration;
        this.verificationLink = verificationLink;
    }

    @Transactional
    @Override
    public void signup(AccountSignupRequestDto signupRequestDto) {
        checkRegex(signupRequestDto.getNickname());
        checkRegex(signupRequestDto.getPassword());
        checkPasswordMatching(signupRequestDto.getPassword(), signupRequestDto.getConfirmPassword());

        if (checkEmailDuplicated(signupRequestDto.getEmail())) {
            throw new DuplicateEmailException();
        }
        if (checkNicknameDuplicated(signupRequestDto.getNickname())) {
            throw new DuplicateNicknameException();
        }

        String salt = saltService.genSalt();
        signupRequestDto.setSalt(salt);
        signupRequestDto.setPassword(saltService.encodePassword(salt, signupRequestDto.getPassword()));

        accountRepository.save(signupRequestDto.toEntity());
    }

    @Override
    public Account login(AccountLoginRequestDto loginRequestDto) {
        Account account = accountRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(AccountNotFoundException::new);
        boolean matches = saltService.matches(loginRequestDto.getPassword(), account.getPassword());
        if (!matches) {
            throw new AccountPasswordNotMatchedException();
        }
        return account;
    }

    @Override
    public void sendSignupVerificationEmail(String email) {
        UUID uuid = UUID.randomUUID();
        redisUtil.setDataExpire(uuid.toString(), email, Long.parseLong(verificationDuration));
        emailService.sendEmail(email, "회원가입 인증 메일입니다.", verificationLink + uuid.toString());
    }

    @Transactional
    @Override
    public void verifyEmail(String key) {
        String email = redisUtil.getData(key);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(AccountNotFoundException::new);

        account.updateEmailVerified();
        redisUtil.deleteData(key);
    }

    @Transactional
    @Override
    public void sendTempPasswordEmail(String email) {
        String uuid = UUID.randomUUID().toString();
        String tempPassword = uuid.substring(0, 15);

        emailService.sendEmail(email, "임시비밀번호 전송 메일입니다.", "로그인 후 패스워드를 변경해주세요. 임시 비밀번호 : " + tempPassword);

        // 패스워드 재설정
        String salt = saltService.genSalt();
        String saltingPassword = saltService.encodePassword(salt, tempPassword);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(AccountNotFoundException::new);
        account.updatePassword(salt, saltingPassword);
        accountRepository.save(account);
    }

    @Transactional
    @Override
    public void updatePassword(Account account, AccountPasswordUpdateRequestDto passwordUpdateRequestDto) {
        String currentPassword = passwordUpdateRequestDto.getCurrentPassword();
        String newPassword = passwordUpdateRequestDto.getNewPassword();
        String confirmNewPassword = passwordUpdateRequestDto.getConfirmNewPassword();

        boolean matches = saltService.matches(currentPassword, account.getPassword());
        if (!matches) {
            throw new AccountPasswordNotMatchedException();
        }

        checkRegex(newPassword);
        checkPasswordMatching(newPassword, confirmNewPassword);

        String salt = saltService.genSalt();
        String saltingPassword = saltService.encodePassword(salt, newPassword);
        account.updatePassword(salt, saltingPassword);
        accountRepository.save(account);
    }

    @Override
    public boolean checkEmailDuplicated(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public boolean checkNicknameDuplicated(String nickname) {
        return accountRepository.existsByNickname(nickname);
    }

    @Override
    public void checkRegex(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                throw new InvalidValueException();
            }
        }
    }

    @Override
    public void checkPasswordMatching(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new InvalidValueException();
        }
    }
}
