package com.community.soob.account.service;

import com.community.soob.account.domain.Account;
import com.community.soob.account.domain.AccountRepository;
import com.community.soob.account.exception.AccountPasswordNotMatchedException;
import com.community.soob.attachment.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountUpdateService {
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final AccountFindService accountFindService;
    private final AccountCheckService accountCheckService;
    private final AttachmentService attachmentService;
    private final SaltService saltService;

    @Transactional
    public void updatePassword(Account account, String currentPassword, String newPassword, String confirmNewPassword) {
        boolean matches = saltService.matches(currentPassword, account.getPassword());
        if (!matches) {
            throw new AccountPasswordNotMatchedException();
        }

        accountCheckService.checkPasswordRegex(newPassword);
        accountCheckService.checkPasswordMatching(newPassword, confirmNewPassword);

        String salt = saltService.genSalt();
        String saltingPassword = saltService.encodePassword(salt, newPassword);
        account.updatePassword(salt, saltingPassword);
        accountRepository.save(account);
    }

    @Transactional
    public void sendTempPasswordEmail(String email) {
        accountCheckService.checkEmailRegex(email);
        Account account = accountFindService.findByEmail(email);
        String tempPassword = UUID.randomUUID().toString();

        emailService.sendEmail(email, "임시비밀번호 전송 메일입니다.", "로그인 후 패스워드를 변경해주세요. 임시 비밀번호 : " + tempPassword);

        // 패스워드 재설정
        String salt = saltService.genSalt();
        String saltingPassword = saltService.encodePassword(salt, tempPassword);

        account.updatePassword(salt, saltingPassword);
        accountRepository.save(account);
    }

    @Transactional
    public void updateAccount(Account account, String nickname, MultipartFile file) {
        accountCheckService.checkNicknameRegex(nickname);

        if (file != null && !file.isEmpty()) {
            if (account.getProfileImage() != null) {
                attachmentService.deleteProfileImage(account);
            }
            attachmentService.uploadProfileImage(account, file);
        }
        account.updateNickname(nickname);
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(long accountId) {
        accountRepository.deleteById(accountId);
    }
}
