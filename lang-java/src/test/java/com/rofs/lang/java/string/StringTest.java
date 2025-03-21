package com.rofs.lang.java.string;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringTest {

    @Test
    void testString() {
        String str = "Hello";
        assertThat(str).isEqualTo("Hello");
    }

    @Test
    void remove() {
        MyStringUtils myStringUtils = new MyStringUtils();
        String str = "Hello-World-100";
        String result = myStringUtils.removeCharacters(str, "-");
        assertThat(result).isEqualTo("HelloWorld100");
    }
}
