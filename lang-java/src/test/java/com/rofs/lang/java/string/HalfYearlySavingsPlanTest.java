package com.rofs.lang.java.string;

import org.junit.jupiter.api.Test;

public class HalfYearlySavingsPlanTest {

    @Test
    public void testCalculateTotalDeposit() {
        int actualTotal = HalfYearlySavingsPlan.calculateTotalDeposit();
        System.out.println("actualTotal = " + actualTotal);
    }
}


class HalfYearlySavingsPlan {

    private static final int WEEKLY_INCREASE = 5000;
    private static final int TOTAL_WEEKS = 26;

    /**
     * Calculates the total deposit amount for the half-yearly savings plan.
     *
     * @return the total deposit amount
     */
    public static int calculateTotalDeposit() {
        int totalDeposit = 0;

        for (int week = 1; week <= TOTAL_WEEKS; week++) {
            int weeklyDeposit = week * WEEKLY_INCREASE;
            totalDeposit += weeklyDeposit;
        }

        return totalDeposit;
    }
}