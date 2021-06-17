package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountFindService {
    private final AccountRepository accountRepository;

    public Account findById(long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
    }

    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(AccountNotFoundException::new);
    }
}
