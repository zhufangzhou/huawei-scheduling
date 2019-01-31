package scheduling.core.input;

public class Capacity {
    private double capacity;
    private double remaining;

    public Capacity(double capacity, double remaining) {
        this.capacity = capacity;
        this.remaining = remaining;
    }

    public Capacity(double capacity) {
        this(capacity, capacity);
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }
}
