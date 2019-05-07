package scheduling.core.input;

import java.sql.Time;

public class TimePeriod {
    private int date;
    private int week;
    private int length;
    private int endDate;

    public TimePeriod(int date, int week, int length) {
        this.date = date;
        this.week = week;
        this.length = length;
        endDate = date + length - 1;
    }

    public int getDate() {
        return date;
    }

    public int getWeek() {
        return week;
    }

    public int getLength() {
        return length;
    }

    public int getEndDate() {
        return endDate;
    }

    /**
     * The due date (integer representation) indicated by the time period.
     * In this version, defined as the first day of the time period.
     * @return the due date.
     */
    public int dueDate() {
        return date;
    }

    /**
     * Get the year of the date (integer representation).
     * @param date the date.
     * @return the year of the date.
     */
    public static int getYear(int date) {
        return Integer.valueOf(String.valueOf(date).substring(0, 4));
    }

    /**
     * Get the month of the date (integer representation).
     * @param date the date.
     * @return the month of the date.
     */
    public static int getMonth(int date) {
        return Integer.valueOf(String.valueOf(date).substring(4, 6));
    }

    /**
     * Get the day in the month of the date (integer representation).
     * @param date the date.
     * @return the day in the month of the date.
     */
    public static int getDay(int date) {
        return Integer.valueOf(String.valueOf(date).substring(6, 8));
    }

    /**
     * Get the gap between two dates. date1 must be earlier than date2.
     * @param date1 the first date.
     * @param date2 the second date.
     * @return the gap between the two dates.
     */
    public static int gap(int date1, int date2) {
        int year1 = getYear(date1);
        int month1 = getMonth(date1);
        int day1 = getDay(date1);
        int year2 = getYear(date2);
        int month2 = getMonth(date2);
        int day2 = getDay(date2);

        int gap = 0;

        int currYear = year1;
        int currMonth = month1;
        int currDay = day1;

        if (currYear < year2) {
            gap += remainingDaysOfYear(currYear, currMonth, currDay);
            currYear++;
            currMonth = 1;
            currDay = 0;
        }

        while (currMonth < month2) {
            gap += getDaysInMonth(currYear, currMonth);
            currMonth ++;
            currDay = 0;
        }

        gap += day2-currDay;

        return gap;
    }

    public static int remainingDaysOfYear(int year, int month, int day) {
        int remainingDays = remainingDaysOfMonth(year, month, day);
        month ++;

        while (month < 13) {
            remainingDays += getDaysInMonth(year, month);
            month ++;
        }

        return remainingDays;
    }

    public static int remainingDaysOfMonth(int year, int month, int day) {
        int totalDays = getDaysInMonth(year, month);

        return totalDays-day;
    }

    /**
     * return the integer representation of a date (year, month, day).
     * @param year the year.
     * @param month the month.
     * @param day the day.
     * @return the integer representation of the date.
     */
    public static int dateOf(int year, int month, int day) {
        String monthStr = String.format("%02d", month);
        String dayStr = String.format("%02d", day);

        return Integer.valueOf(year + monthStr + dayStr);
    }

    /**
     * The next date (integer representation) of a date.
     * @param date the date.
     * @return the next date.
     */
    public static int nextDate(int date) {
        int year = getYear(date);
        int month = getMonth(date);
        int day = getDay(date);

        day ++;

        if (day > getDaysInMonth(month, year)) {
            month ++;
            day = 1;

            if (month > 12) {
                year ++;
                month = 1;
            }
        }

        return dateOf(year, month, day);
    }

    public static int getDaysInMonth(int year, int month) {
        int days = 0;
        if (month == 1 || month == 3 || month == 5 || month == 7 ||
                month == 8 || month == 10 || month == 12) {
            days = 31;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            days = 30;
        } else if (month == 2) {
            if (year % 4 == 0) {
                days = 29;
            } else {
                days = 28;
            }
        }

        return days;
    }

//    public static void main(String[] args) {
//        int date1 = 20161201;
//        int date2 = 20170301;
//
//        System.out.println(TimePeriod.gap(date1, date2));
//    }
}
