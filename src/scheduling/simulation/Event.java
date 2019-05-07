package scheduling.simulation;

public abstract class Event implements Comparable<Event> {
    protected int dateId;

    public Event(int dateId) {
        this.dateId = dateId;
    }

    public int getDateId() {
        return dateId;
    }

    public abstract void trigger(State state);

    @Override
    public int compareTo(Event o) {
        if (dateId < o.dateId)
            return -1;

        if (dateId > o.dateId)
            return 1;

        return 0;
    }
}
