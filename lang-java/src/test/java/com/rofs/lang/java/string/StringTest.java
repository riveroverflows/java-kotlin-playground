package com.rofs.lang.java.string;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

public class StringTest {

    @Test
    void containsAllTest() {
        // 2,3 containsall (1,2,3)
        List<String> list1 = List.of("1", "2", "3"); // 1,2,3 containsall (2,3) true
        List<String> list2 = List.of("2", "3"); // 2,3 containsall (1,2,3) false
        System.out.println(list1.containsAll(list2));
        System.out.println(list2.containsAll(list1));
    }


    @Test
    void textBlockTest() {
        String textBlock = """
                           Hello, World!
                           This is a text block.
                           """;
        String normalText = "Hello, World!\nThis is a text block.\n";

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String s = objectMapper.writeValueAsString(textBlock);
            System.out.println("s = " + s);
        } catch (JsonProcessingException ignore) {
        }
        System.out.println(textBlock);
        System.out.println(normalText);
    }

    @Test
    void splitTest() {
        String str = "10000";
        List<String> list = split(str, "\\.");
        System.out.println(list);
    }

    private String removeWhitespaces(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("\\s+", "");
    }

    private boolean hasText(String str) {
        return StringUtils.hasText(removeWhitespaces(str));
    }


    private List<String> split(String str, String separator) {
        if (!hasText(str)) {
            return List.of();
        }
        String defaultSeparator = ",";
        String _separator = hasText(separator) ? separator : defaultSeparator;
        return Arrays.asList(str.split(_separator));
    }

    @Test
    void tt() {
        String text = "Hello\u2003World\u200B👋";  // 유니코드 공백과 이모지
        System.out.println(text);  // "Hello World👋" (중간에 보이지 않는 공백들이 있음)

// 기존 방식으로는 일부 유니코드 공백이 남을 수 있음
        String l = text.replaceAll("\\s+", "");
        System.out.println(l);  // "HelloWorld👋" (일부 공백이 남을 수 있음)
        System.out.println(l.length());

// codePoints 방식으로는 모든 종류의 공백을 정확히 제거
        String x = newRemoveWhitespaces(text);
        System.out.println(x);  // "HelloWorld👋"
        System.out.println(x.length());
    }

    public static String newRemoveWhitespaces(String str) {
        return str == null ? "" :
               str.codePoints()
                  .filter(cp -> !Character.isWhitespace(cp))
                  .mapToObj(Character::toString)
                  .collect(Collectors.joining());
    }

    @Test
    void ttt() {
        String text = "Hello 👋 World";  // 이모지를 포함한 문자열

// 잘못된 처리 방식
        System.out.println(text.length());  // 14 (이모지가 2개의 char로 계산됨)

// 올바른 처리 방식
        System.out.println(text.codePoints().count()); // 13
    }

}
