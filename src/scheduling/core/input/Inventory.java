package scheduling.core.input;

/**
 * The inventory class.
 * The "total" field means the current total inventory.
 * The "free" field means the free inventory that can be supplied.
 * The two can be different because some inventory has been allocated to some future supplies.
 */
public class Inventory {
    private double total;
    private double free;

    public Inventory(double total, double free) {
        this.total = total;
        this.free = free;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getFree() {
        return free;
    }

    public void setFree(double free) {
        this.free = free;
    }

    public void add(double quantity) {
        total += quantity;
        free += quantity;
    }

    public void remove(double quantity) {
        total -= quantity;
        free -= quantity;
    }

    public void addFree(double quantity) {
        free += quantity;
    }

    public void removeFree(double quantity) {
        free -= quantity;
    }

    @Override
    public String toString() {
        return "{" + total + ", " + free + '}';
    }
}
