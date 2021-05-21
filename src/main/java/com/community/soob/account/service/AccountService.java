package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {
    Account findById(long accountId);
    void deleteAccount(long accountId);
    boolean checkEmailDuplicated(String email);
    boolean checkNicknameDuplicated(String nickname);
    void updateAccount(Account account, String nickname, MultipartFile file);
}
