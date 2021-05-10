package com.community.soob.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProfileController {
    private final Environment environment;

    @GetMapping("/profile")
    public String profile() {
        /*
            현재 실행중인 ActiveProfile 을 모두 가져옴
            만약 real, oauth, real-db 등이 활성화 되어 있다면 3개가 담겨있음
            여기서 real, real1, real2 는 모두 배포에 사용될 profile 이라 이 중 하나라도 있으면 그 값을 반환하도록
            실제로는 real1, real2 만 사용하지만 step2 를 다시 사용해볼 수 있으니 real 도 남겨둔다.
         */
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        List<String> productionProfiles = Arrays.asList("production1", "production2");
        String defaultProfile = profiles.isEmpty() ? "development" : profiles.get(0);

        return profiles.stream()
                .filter(productionProfiles::contains)
                .findAny()
                .orElse(defaultProfile);
    }
}
