package com.community.soob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class SoobApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SoobApplication.class);
        springApplication.setDefaultProperties(Map.of("spring.profiles.active", "development"));
        springApplication.run(args);
    }
}
