package com.rofs.lang.java.string;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeTest {

    @Test
    void compareDateTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime later = now.plusMinutes(10);
        LocalDateTime before = now.minusMinutes(10);
        LocalDateTime completedAt = LocalDateTime.of(2025, 10, 25, 14, 30, 0);
        LocalDate completedDate = completedAt.toLocalDate();
        int cooldownDays = 3;
        LocalDate cooldownEndDate = completedDate.plusDays(cooldownDays);

        System.out.println("Now: " + now);
        System.out.println("Today: " + today);

        System.out.println("======================");

        System.out.println("Completed At: " + completedAt);
        System.out.println("Completed Date: " + completedDate);
        System.out.println("Cooldown End Date: " + cooldownEndDate);

        System.out.println("=====================");

        System.out.println("Is today before cooldown end date? " + today.isBefore(cooldownEndDate));

        assertThat(later.isAfter(now)).isTrue();
        assertThat(before.isBefore(now)).isTrue();
    }
}
