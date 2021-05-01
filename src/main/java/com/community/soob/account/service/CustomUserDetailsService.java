package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.domain.UserAccount;
import com.community.soob.account.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String accountEmail) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(accountEmail)
                .orElseThrow(AccountNotFoundException::new);
        return new UserAccount(account);
    }
}
