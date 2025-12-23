package com.rofs.myhome.etc;

import java.util.Scanner;

public class MyHomeUtils {
    public static void printLineAsCount(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println();
        }
    }

    public static void delayAsMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    public static boolean isInteger(String str) {
        return str != null && str.matches("^\\d+$");
    }

    public static void enterAgain(Scanner scanner) {
        printLineAsCount(100);
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("            잘못 입력했어요. 다시 입력해주세요.");
        scanner.nextLine();
    }

    public static String input(Scanner scanner) {
        String input = scanner.next();
        scanner.nextLine();
        return input;
    }

    public static int stringToInt(String stringNumber) {
        return Integer.parseInt(stringNumber);
    }
}
