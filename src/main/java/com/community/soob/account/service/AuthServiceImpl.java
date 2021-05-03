package com.community.soob.account.service;

import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountNotFoundException;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import com.community.soob.config.properties.SettingsProperties;
import com.community.soob.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final SaltService saltService;
    private final SettingsProperties settingsProperties;
    private final RedisUtil redisUtil;

    @Transactional
    @Override
    public void signup(AccountSignupRequestDto signupRequestDto) {
        String password = signupRequestDto.getPassword();
        String salt = saltService.genSalt();

        signupRequestDto.setSalt(salt);
        signupRequestDto.setPassword(saltService.encodePassword(salt, password));

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
        String verificationLink = settingsProperties.getEmailProperties().getVerificationLink();
        long duration = settingsProperties.getEmailProperties().getVerificationDuration();

        UUID uuid = UUID.randomUUID();
        redisUtil.setDataExpire(uuid.toString(), email, duration);
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

    @Override
    public void logout() {

    }
}
