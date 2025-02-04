package com.github.sculkhorde.util;

public class TickUnits {


    public static final int TICKS_PER_SECOND = 20;

    public static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;

    public static final int TICKS_PER_HOUR = TICKS_PER_MINUTE * 60;

    public static final int TICKS_PER_DAY = TICKS_PER_HOUR * 24;

    public static final int TICKS_PER_WEEK = TICKS_PER_DAY * 7;

    public static final int TICKS_PER_MONTH = TICKS_PER_DAY * 30;

    public static final int TICKS_PER_YEAR = TICKS_PER_DAY * 365;

    public static int convertSecondsToTicks(float seconds) {
        return (int) (seconds * TICKS_PER_SECOND);
    }

    public static long convertTicksToSeconds(long ticks) { return ticks/TICKS_PER_SECOND; }

    public static int convertSecondsToTicks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    public static int convertMinutesToTicks(int minutes) {
        return minutes * TICKS_PER_MINUTE;
    }

    public static long convertTicksToMinutes(long ticks) { return ticks/TICKS_PER_MINUTE; }

    public static int convertHoursToTicks(int hours) {
        return hours * TICKS_PER_HOUR;
    }

    public static int convertDaysToTicks(int days) {
        return days * TICKS_PER_DAY;
    }

    public static int convertWeeksToTicks(int weeks) {
        return weeks * TICKS_PER_WEEK;
    }

    public static int convertMonthsToTicks(int months) {
        return months * TICKS_PER_MONTH;
    }

    public static int convertYearsToTicks(int years) {
        return years * TICKS_PER_YEAR;
    }
}
