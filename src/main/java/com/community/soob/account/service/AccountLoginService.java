package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountLoginService {
    private final AccountFindService accountFindService;
    private final SaltService saltService;

    public Account login(String email, String password) {
        Account account = accountFindService.findByEmail(email);
        boolean matches = saltService.matches(password, account.getPassword());
        if (!matches) {
            throw new AccountPasswordNotMatchedException();
        }
        return account;
    }
}
