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
import java.util.regex.Pattern;

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
        checkEmailRegex(signupRequestDto.getEmail());
        checkNicknameRegex(signupRequestDto.getNickname());
        checkPasswordRegex(signupRequestDto.getPassword());
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
        checkEmailRegex(email);
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
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(AccountNotFoundException::new);
        String tempPassword = UUID.randomUUID().toString();

        emailService.sendEmail(email, "임시비밀번호 전송 메일입니다.", "로그인 후 패스워드를 변경해주세요. 임시 비밀번호 : " + tempPassword);

        // 패스워드 재설정
        String salt = saltService.genSalt();
        String saltingPassword = saltService.encodePassword(salt, tempPassword);

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

        checkPasswordRegex(newPassword);
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
    public void checkEmailRegex(String email) {
        // 이메일 형식 체크
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(regex, email)) {
            throw new InvalidEmailException();
        }
    }

    @Override
    public void checkNicknameRegex(String nickname) {
        // 문자, 숫자만 가능
        for (int i = 0; i < nickname.length(); i++) {
            char c = nickname.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                throw new InvalidNicknameException();
            }
        }
    }

    @Override
    public void checkPasswordRegex(String password) {
        // 문자, 숫자, 특수문자 12자 이상
        String regex = "^[a-zA-Z0-9~!@#$%^&*()_+=.-]{12,}$";
        if (!Pattern.matches(regex, password)) {
            throw new InvalidPasswordException();
        }
    }

    @Override
    public void checkPasswordMatching(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new AccountPasswordNotMatchedException();
        }
    }
}
