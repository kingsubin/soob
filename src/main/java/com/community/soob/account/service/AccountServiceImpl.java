package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountNotFoundException;
import com.community.soob.attachment.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AttachmentService attachmentService;
    private final AuthService authService;

    @Value("{attachment.url.profile}")
    private String directoryName;

    @Override
    public Account findById(long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    @Override
    public void updateAccount(Account account, String nickname, MultipartFile file) {
        authService.checkRegex(nickname);

        if (!file.isEmpty()) {
            attachmentService.deleteProfileImage(account);
            attachmentService.uploadProfileImage(account, file, directoryName);
        }
        account.updateNickname(nickname);
        accountRepository.save(account);
    }

    @Transactional
    @Override
    public void deleteAccount(long accountId) {
        accountRepository.deleteById(accountId);
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
