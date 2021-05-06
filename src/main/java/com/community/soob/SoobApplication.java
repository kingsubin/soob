package com.community.soob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SoobApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SoobApplication.class);
        springApplication.setAdditionalProfiles("development");
        springApplication.run(args);
    }
}
