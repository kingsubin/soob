package com.community.soob.account.domain;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserAccount extends User {
    private final Account account;

    public UserAccount(Account account) {
        super(account.getEmail(), account.getPassword(), List.of(new SimpleGrantedAuthority(account.getRole().name())));
        this.account = account;
    }
}
