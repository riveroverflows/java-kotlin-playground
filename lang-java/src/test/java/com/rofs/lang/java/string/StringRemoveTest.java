package com.rofs.lang.java.string;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

public class StringRemoveTest {

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
