package scheduling.core.simulation;

public abstract class Event implements Comparable<Event> {
    protected int date;

    public Event(int date) {
        this.date = date;
    }

    public int getDate() {
        return date;
    }

    public abstract void trigger(Simulator simulator);

    @Override
    public int compareTo(Event o) {
        if (date < o.date)
            return -1;

        if (date > o.date)
            return 1;

        return 0;
    }
}
