package com.r11s.eventdriven;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EventDrivenApplicationTests {

    @Test
    void contextLoads() {
    }

}
