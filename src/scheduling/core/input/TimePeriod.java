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
}
