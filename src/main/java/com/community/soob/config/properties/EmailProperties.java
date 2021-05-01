package com.community.soob.config.properties;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailProperties {
    private Long verificationDuration;
    private String verificationLink;
}
