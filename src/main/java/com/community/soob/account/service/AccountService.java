package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.attachment.Attachment;

public interface AccountService {
    Account findById(long accountId);
    void deleteAccount(long accountId);
    boolean checkEmailDuplicated(String email);
    boolean checkNicknameDuplicated(String nickname);
    void updateNickname(Account account, String nickname);
    void updateProfileImage(Account account, Attachment attachment);
}
