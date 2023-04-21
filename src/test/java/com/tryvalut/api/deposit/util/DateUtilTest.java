package com.tryvalut.api.deposit.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void testGetStartAndEndOfWeek() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 4, 20, 12, 0, 0);
        Pair<LocalDateTime, LocalDateTime> result = DateUtil.getStartAndEndOfWeek(dateTime);
        LocalDateTime expectedStartOfWeek = LocalDateTime.of(2023, 4, 17, 0, 0, 0);
        LocalDateTime expectedEndOfWeek = LocalDateTime.of(2023, 4, 23, 23, 59, 59);
        assertEquals(expectedStartOfWeek, result.getFirst(), "Start of week should be " + expectedStartOfWeek);
        assertEquals(expectedEndOfWeek, result.getSecond(), "End of week should be " + expectedEndOfWeek);
    }

    @Test
    void testGetStartAndEndOfWeek_withMonday() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 4, 17, 12, 0, 0);
        Pair<LocalDateTime, LocalDateTime> result = DateUtil.getStartAndEndOfWeek(dateTime);
        LocalDateTime expectedStartOfWeek = LocalDateTime.of(2023, 4, 17, 0, 0, 0);
        LocalDateTime expectedEndOfWeek = LocalDateTime.of(2023, 4, 23, 23, 59, 59);
        assertEquals(expectedStartOfWeek, result.getFirst(), "Start of week should be " + expectedStartOfWeek);
        assertEquals(expectedEndOfWeek, result.getSecond(), "End of week should be " + expectedEndOfWeek);
    }

    @Test
    void testGetStartAndEndOfWeek_withNullInput() {
        assertThrows(NullPointerException.class, () -> DateUtil.getStartAndEndOfWeek(null), "Should throw NullPointerException if input is null");
    }

    @Test
    void testGetStartAndEndOfWeek_withSunday() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 4, 23, 12, 0, 0);
        Pair<LocalDateTime, LocalDateTime> result = DateUtil.getStartAndEndOfWeek(dateTime);
        LocalDateTime expectedStartOfWeek = LocalDateTime.of(2023, 4, 17, 0, 0, 0);
        LocalDateTime expectedEndOfWeek = LocalDateTime.of(2023, 4, 23, 23, 59, 59);
        assertEquals(expectedStartOfWeek, result.getFirst(), "Start of week should be " + expectedStartOfWeek);
        assertEquals(expectedEndOfWeek, result.getSecond(), "End of week should be " + expectedEndOfWeek);
    }

    @Test
    void testGetStartAndEndOfWeek_withDaylightSavingTime() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 29, 12, 0, 0);
        Pair<LocalDateTime, LocalDateTime> result = DateUtil.getStartAndEndOfWeek(dateTime);
        LocalDateTime expectedStartOfWeek = LocalDateTime.of(2023, 10, 23, 0, 0, 0);
        LocalDateTime expectedEndOfWeek = LocalDateTime.of(2023, 10, 29, 23, 59, 59);
        assertEquals(expectedStartOfWeek, result.getFirst(), "Start of week should be " + expectedStartOfWeek);
        assertEquals(expectedEndOfWeek, result.getSecond(), "End of week should be " + expectedEndOfWeek);
    }

    @Test
    void testGetStartAndEndOfDay() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 4, 20, 12, 0, 0);
        Pair<LocalDateTime, LocalDateTime> result = DateUtil.getStartAndEndOfDay(dateTime);
        LocalDate date = dateTime.toLocalDate();
        LocalDateTime expectedStartOfDay = date.atStartOfDay();
        LocalDateTime expectedEndOfDay = date.atTime(23, 59, 59);
        assertEquals(expectedStartOfDay, result.getFirst(), "Start of day should be " + expectedStartOfDay);
        assertEquals(expectedEndOfDay, result.getSecond(), "End of day should be " + expectedEndOfDay);
    }

    @Test
    void testGetStartAndEndOfDay_withNullInput() {
        assertThrows(NullPointerException.class, () -> DateUtil.getStartAndEndOfDay(null), "Should throw NullPointerException if input is null");
    }

    @Test
    void testGetStartAndEndOfDay_withLeapYear() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 29, 12, 0, 0);
        Pair<LocalDateTime, LocalDateTime> result = DateUtil.getStartAndEndOfDay(dateTime);
        LocalDate date = dateTime.toLocalDate();
        LocalDateTime expectedStartOfDay = date.atStartOfDay();
        LocalDateTime expectedEndOfDay = date.atTime(23, 59, 59);
        assertEquals(expectedStartOfDay, result.getFirst(), "Start of day should be " + expectedStartOfDay);
        assertEquals(expectedEndOfDay, result.getSecond(), "End of day should be " + expectedEndOfDay);
    }

    @Test
    void testGetStartAndEndOfDay_withDaylightSavingTime() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 29, 1, 30, 0);
        Pair<LocalDateTime, LocalDateTime> result = DateUtil.getStartAndEndOfDay(dateTime);
        LocalDate date = dateTime.toLocalDate();
        LocalDateTime expectedStartOfDay = date.atStartOfDay();
        LocalDateTime expectedEndOfDay = date.atTime(23, 59, 59);
        assertEquals(expectedStartOfDay, result.getFirst(), "Start of day should be " + expectedStartOfDay);
        assertEquals(expectedEndOfDay, result.getSecond(), "End of day should be " + expectedEndOfDay);
    }
}