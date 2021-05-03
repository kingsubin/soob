package com.community.soob.account.service;

import com.community.soob.account.controller.dto.AccountLoginRequestDto;
import com.community.soob.account.controller.dto.AccountSignupRequestDto;
import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountNotFoundException;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final SaltService saltService;

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

    }

    @Override
    public void verifyEmail(String email) {

    }

    @Override
    public void sendTempPasswordEmail(String email) {

    }

    @Override
    public void logout() {

    }
}
