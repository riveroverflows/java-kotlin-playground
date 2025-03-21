package com.rofs.lang.java.string;

import java.util.Set;
import java.util.stream.Collectors;

public class MyStringUtils {

    public String removeWhitespaces(String str) {
        return str == null ? "" :
               str.codePoints()
                  .filter(cp -> !Character.isWhitespace(cp))
                  .mapToObj(Character::toString)
                  .collect(Collectors.joining());
    }

    public String removeCharacters(String input, String charactersToRemove) {
        if (input == null) {
            return "";
        }

        Set<Integer> removalSet = charactersToRemove.codePoints().boxed().collect(Collectors.toSet());
        return input.codePoints()
                    .filter(codePoint -> !removalSet.contains(codePoint))
                    .mapToObj(Character::toString)
                    .collect(Collectors.joining());
    }
}
