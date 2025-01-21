package com.rofs.lang.java.calculator;

import org.junit.jupiter.api.Test;

public class kakao26주적금액계산 {

    @Test
    void test() {
        int price = 5000;
        int sum = 0;
        for (int i = 1; i <= 26; i++) {
            sum += price * i;
        }
        System.out.println("sum = " + sum);
        double month = (double) sum / 6;
        System.out.println("한달에 낼 금액 = " + month);
    }
}
