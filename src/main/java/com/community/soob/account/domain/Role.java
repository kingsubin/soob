package com.community.soob.account.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
    NOT_PERMITTED(0, "NOT_PERMITTED"),
    LEVEL_1(1, "LEVEL_1"),
    LEVEL_2(2, "LEVEL_2"),
    LEVEL_3(3, "LEVEL_3"),
    MANAGER(4, "MANAGER"),
    ADMIN(5, "ADMIN"),
    ;

    private final int level;
    private final String title;
}
