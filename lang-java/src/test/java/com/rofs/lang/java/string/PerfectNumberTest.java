package com.rofs.lang.java.string;

import org.junit.jupiter.api.Test;

public class PerfectNumberTest {

    @Test
    void test() {
        int r = 0;
        for (int i = 1; i <= 100; i++) {
            System.out.println("[START] =========== num ==========> " + i);
            if (isPerfectNumber(i)) {
                r += i;
            }
            System.out.println("[  END] =========== num ==========> " + i);
            System.out.println("\n===============================================\n");
        }
        System.out.println("Perfect Number Sum ====> " + r);
    }

    boolean isPerfectNumber(int num) {
        int sum = 0;
        for (int i = 1; i < num; i++) {
            if (sum > num) {
                System.out.println("[이미 완전수가 아님] =========== sum > num ====> " + sum);
                break;
            }
            if (num % i == 0) {
                System.out.println("num % i is true ====> " + i);
                sum += i;
            }
        }
        System.out.println("[  SUM] ====== sum ====> " + sum);
        if (num == sum) {
            System.out.println("[Perfect Number] ===== num ====> " + num);
            return true;
        }
        return false;
    }
}
