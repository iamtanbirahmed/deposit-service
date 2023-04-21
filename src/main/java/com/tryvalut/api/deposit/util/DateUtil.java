package com.tryvalut.api.deposit.util;


import com.tryvalut.api.deposit.service.DepositService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class DateUtil {

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public static Pair<LocalDateTime, LocalDateTime> getStartAndEndOfDay(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        logger.debug(" Start of day: {}; end of day {}", startOfDay, endOfDay);
        return Pair.of(startOfDay, endOfDay);
    }

    public static Pair<LocalDateTime, LocalDateTime> getStartAndEndOfWeek(LocalDateTime dateTime) {

        DayOfWeek currentDayOfWeek = dateTime.getDayOfWeek();
        LocalDateTime startOfWeek = null;
        if (currentDayOfWeek == DayOfWeek.MONDAY) {
            startOfWeek = dateTime.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        } else {
            int daysToMonday = DayOfWeek.MONDAY.getValue() - currentDayOfWeek.getValue();
            if (daysToMonday > 0) {
                daysToMonday -= 7;
            }
            startOfWeek = dateTime.plusDays(daysToMonday).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        }
        LocalDateTime startOfNextWeek = startOfWeek.plusDays(7);

        LocalDateTime endOfWeek = startOfNextWeek.minusSeconds(1);
        logger.debug(" Start of week: {}; end of week {}", startOfWeek, endOfWeek);
        return Pair.of(startOfWeek, endOfWeek);
    }
}
