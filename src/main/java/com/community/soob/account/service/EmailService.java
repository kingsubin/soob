package com.community.soob.account.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
