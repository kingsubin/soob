package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountNotFoundException;
import com.community.soob.attachment.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public Account findById(long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    @Override
    public void deleteAccount(long accountId) {
        accountRepository.deleteById(accountId);
    }

    @Transactional
    @Override
    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
    }

    @Transactional
    @Override
    public void updateProfileImage(Account account, Attachment attachment) {
        account.setProfileImage(attachment);
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
}
