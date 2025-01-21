package com.rofs.lang.java.list;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ListTest {

    @Test
    void addEmptyList() {
        List<String> finalList = new ArrayList<>();
        finalList.add("1");
        finalList.add("2");
        finalList.add("3");

        List<String> emptyList = new ArrayList<>();
        finalList.addAll(emptyList);

        System.out.println(finalList);
    }
}
