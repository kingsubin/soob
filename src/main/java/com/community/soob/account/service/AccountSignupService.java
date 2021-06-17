package com.community.soob.account.service;

import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.DuplicateEmailException;
import com.community.soob.account.exception.DuplicateNicknameException;
import com.community.soob.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional(readOnly = true)
@Service
public class AccountSignupService {
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final AccountCheckService accountCheckService;
    private final AccountFindService accountFindService;
    private final SaltService saltService;
    private final RedisUtil redisUtil;
    private final String verificationDuration;
    private final String verificationLink;

    public AccountSignupService(AccountRepository accountRepository, EmailService emailService, AccountCheckService accountCheckService, AccountFindService accountFindService, SaltService saltService, RedisUtil redisUtil, @Value("{email.verification.duration}") String verificationDuration, @Value("{email.verification.link}") String verificationLink) {
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.accountCheckService = accountCheckService;
        this.accountFindService = accountFindService;
        this.saltService = saltService;
        this.redisUtil = redisUtil;
        this.verificationDuration = verificationDuration;
        this.verificationLink = verificationLink;
    }

    @Transactional
    public void signup(AccountSignupRequestDto signupRequestDto) {
        accountCheckService.checkEmailRegex(signupRequestDto.getEmail());
        accountCheckService.checkNicknameRegex(signupRequestDto.getNickname());
        accountCheckService.checkPasswordRegex(signupRequestDto.getPassword());
        accountCheckService.checkPasswordMatching(signupRequestDto.getPassword(), signupRequestDto.getConfirmPassword());

        if (accountCheckService.checkEmailDuplicated(signupRequestDto.getEmail())) {
            throw new DuplicateEmailException();
        }
        if (accountCheckService.checkNicknameDuplicated(signupRequestDto.getNickname())) {
            throw new DuplicateNicknameException();
        }

        String salt = saltService.genSalt();
        signupRequestDto.setSalt(salt);
        signupRequestDto.setPassword(saltService.encodePassword(salt, signupRequestDto.getPassword()));

        accountRepository.save(signupRequestDto.toEntity());
    }

    public void sendSignupVerificationEmail(String email) {
        accountCheckService.checkEmailRegex(email);

        UUID uuid = UUID.randomUUID();
        redisUtil.setDataExpire(uuid.toString(), email, Long.parseLong(verificationDuration));
        emailService.sendEmail(email, "회원가입 인증 메일입니다.", verificationLink + uuid.toString());
    }

    @Transactional
    public void verifyEmail(String key) {
        String email = redisUtil.getData(key);
        Account account = accountFindService.findByEmail(email);

        account.updateEmailVerified();
        redisUtil.deleteData(key);
    }
}
