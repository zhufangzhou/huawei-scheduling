package scheduling.core.input;

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

    public static int getYear(int date) {
        return Integer.valueOf(String.valueOf(date).substring(0, 4));
    }

    public static int getMonth(int date) {
        return Integer.valueOf(String.valueOf(date).substring(4, 6));
    }

    public static int getDay(int date) {
        return Integer.valueOf(String.valueOf(date).substring(6, 8));
    }

    public static int dateOf(int year, int month, int day) {
        String monthStr = String.format("%02d", month);
        String dayStr = String.format("%02d", day);

        return Integer.valueOf(year + monthStr + dayStr);
    }

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

    public static int getDaysInMonth(int month, int year) {
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
}
