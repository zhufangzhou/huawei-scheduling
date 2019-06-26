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
    protected double quantity;
    protected double type;

    protected double priority;

    public Demand(int dateId, Item item, double quantity) {
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void addQuantity(double quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(double quantity) {
        this.quantity -= quantity;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * Get the latest supply date (exclusive).
     * For order demand, it is the last day of the schedule.
     * For forecast demand, it is only the current day
     * @param schedule the schedule.
     * @return the latest supply date
     */
    public abstract int latestSupplyDate(Schedule schedule);

    /**
     * Supply the demand, update the schedule.
     * @param supplyChain the supply chain to supply the demand.
     * @param suppQuantity the supplied quantity.
     * @param schedule the schedule to be updated.
     */
    public abstract void supplied(SupplyChain supplyChain, double suppQuantity, Schedule schedule);

    @Override
    public String toString() {
        return "[" + dateId + ", " + item.toString() + ", " + quantity + "]";
    }

    @Override
    public int compareTo(Demand o) {
        // compare the requested date id
        if (priority < o.priority)
            return -1;

        if (priority > o.priority)
            return 1;

        return item.compareTo(o.item);
    }
}
