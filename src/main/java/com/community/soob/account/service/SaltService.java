package com.community.soob.account.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class SaltService {
    public String encodePassword(String salt, String password) {
        return BCrypt.hashpw(password,salt);
    }

    public String genSalt() {
        return BCrypt.gensalt();
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
