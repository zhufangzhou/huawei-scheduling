package scheduling.core.input;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;

/**
 * A demand includes
 *   - the requested date id
 *   - the requested item
 *   - the quantity requested
 */

public abstract class Demand implements Comparable<Demand> {
    protected int dateId;
    protected Item item;
    protected long quantity;

    protected double priority;

    public Demand(int dateId, Item item, long quantity) {
        this.dateId = dateId;
        this.item = item;
        this.quantity = quantity;
    }

    public int getDateId() {
        return dateId;
    }

    public void setDateId(int dateId) {
        this.dateId = dateId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void addQuantity(long quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(long quantity) {
        this.quantity -= quantity;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public abstract void supplied(SupplyChain supplyChain, long suppQuantity, Schedule schedule);

    @Override
    public String toString() {
        return "[" + dateId + ", " + item.toString() + ", " + quantity + "]";
    }

    @Override
    public int compareTo(Demand o) {
        // compare the requested date id
        if (dateId < o.dateId)
            return -1;

        if (dateId > o.dateId)
            return 1;

        return item.compareTo(o.item);
    }
}
