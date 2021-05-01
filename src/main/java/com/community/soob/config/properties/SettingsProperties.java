package com.community.soob.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "settings")
public class SettingsProperties {
    private EmailProperties emailProperties;
    private AttachmentProperties attachmentProperties;
}
