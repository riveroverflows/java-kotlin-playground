package com.rofs.lang.java.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MapAndFlatMapTest {

    List<List<String>> list;

    @BeforeEach
    void setUp() {
        System.out.println("setUp");
        list = Arrays.asList(
            List.of("a"),
            List.of("b"));
    }

    @Test
    void mapInOptional() {
        Optional<String> s = Optional.of("test");
        System.out.println("s = " + s);
        assertEquals(Optional.of("TEST"), s.map(String::toUpperCase));
    }

    @Test
    void nestedOptionalString() {
        Optional<Optional<String>> nestedOptionalString1 = Optional.of(Optional.of("STRING"));
        Optional<Optional<String>> nestedOptionalString2 = Optional.of("com/rofs/lang/java/string").map(s -> Optional.of("STRING"));
        System.out.println("nestedOptionalString1 = " + nestedOptionalString1);
        System.out.println("nestedOptionalString2 = " + nestedOptionalString2);
        assertEquals(nestedOptionalString1, nestedOptionalString2);
    }

    @Test
    void flatMapInOptional() {
        Optional<String> flatMapString = Optional.of("com/rofs/lang/java/string").flatMap(s -> Optional.of("STRING"));
        System.out.println("flatMapString = " + flatMapString);
        assertEquals(Optional.of("STRING"), flatMapString);
    }

    @Test
    void flatMapInStream() {
        System.out.println("list = " + list);
        List<String> flatMapList = list.stream()
                                       .flatMap(Collection::stream)
                                       .toList();
        System.out.println("flatMapList = " + flatMapList);
    }
}
