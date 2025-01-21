package com.rofs.lang.java.string;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class StringTest {

    @Test
    void testString() {
        String str = "Hello";
        assertThat(str).isEqualTo("Hello");
    }
}
