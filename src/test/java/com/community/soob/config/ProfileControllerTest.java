package com.community.soob.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileControllerTest {

    @Test
    void production_profile이_조회된다() {
        // given
        String expectedProfile = "production1";
        MockEnvironment environment = new MockEnvironment();
        environment.addActiveProfile(expectedProfile);
        environment.addActiveProfile("mail");
        environment.addActiveProfile("production-db");
        environment.addActiveProfile("aws");

        ProfileController profileController = new ProfileController(environment);

        // when
        String profile = profileController.profile();

        // then
        assertThat(profile).isEqualTo(expectedProfile);
    }

    @Test
    void production_profile이_없으면_첫번째가_조회된다() {
        // given
        String expectedProfile = "mail";
        MockEnvironment environment = new MockEnvironment();

        environment.addActiveProfile(expectedProfile);
        environment.addActiveProfile("production-db");

        ProfileController profileController = new ProfileController(environment);

        // when
        String profile = profileController.profile();

        // then
        assertThat(profile).isEqualTo(expectedProfile);
    }

    @Test
    void active_profile이_없으면_development가_조회된다() {
        // given
        String expectedProfile = "development";
        MockEnvironment environment = new MockEnvironment();
        ProfileController profileController = new ProfileController(environment);

        // when
        String profile = profileController.profile();

        // then
        assertThat(profile).isEqualTo(expectedProfile);
    }

}
