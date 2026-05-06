package com.eams;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EamsApplicationTests {

    @Test
    void contextLoads() {
        // Context load test — runs with application-test.yml profile
    }

}