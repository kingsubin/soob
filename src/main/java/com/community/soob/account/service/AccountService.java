package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {
    Account findById(long accountId);
    void deleteAccount(long accountId);
    void updateAccount(Account account, String nickname, MultipartFile file);
}
