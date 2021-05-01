package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.attachment.Attachment;

public interface AccountService {
    Account findById(long accountId);
    void deleteAccount(long accountId);
    void updateProfileImage(Account account, Attachment attachment);
    boolean checkEmailDuplicated(String email);
    boolean checkNicknameDuplicated(String nickname);
}
