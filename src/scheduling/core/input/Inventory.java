package scheduling.core.input;

/**
 * The inventory class.
 * The "total" field means the current total inventory.
 * The "free" field means the free inventory that can be supplied.
 * The two can be different because some inventory has been allocated to some future supplies.
 */
public class Inventory {
    private long total;
    private long free;

    public Inventory(long total, long free) {
        this.total = total;
        this.free = free;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getFree() {
        return free;
    }

    public void setFree(long free) {
        this.free = free;
    }

    public void add(long quantity) {
        total += quantity;
        free += quantity;
    }

    public void remove(long quantity) {
        total -= quantity;
        free -= quantity;
    }

    public void addFree(long quantity) {
        free += quantity;
    }

    public void removeFree(long quantity) {
        free -= quantity;
    }

    @Override
    public String toString() {
        return "{" + total + ", " + free + '}';
    }
}
