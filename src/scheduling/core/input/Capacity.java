package scheduling.core.input;

/**
 * The capacity of a machine.
 * It has the total capacity, and the remaining capacity (after allocating the productions).
 */

public class Capacity {
    private double total;
    private double remaining;

    public Capacity(double total, double remaining) {
        this.total = total;
        this.remaining = remaining;
    }

    public Capacity(double capacity) {
        this(capacity, capacity);
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }
}
