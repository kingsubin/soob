package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.attachment.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountServiceImpl implements AccountService {
    @Override
    public Account findById(long accountId) {
        return null;
    }

    @Override
    public void deleteAccount(long accountId) {

    }

    @Override
    public void updateProfileImage(Account account, Attachment attachment) {

    }

    @Override
    public boolean checkEmailDuplicated(String email) {
        return false;
    }

    @Override
    public boolean checkNicknameDuplicated(String nickname) {
        return false;
    }
}
