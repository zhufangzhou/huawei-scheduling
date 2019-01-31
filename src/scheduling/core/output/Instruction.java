package scheduling.core.output;

import scheduling.core.input.Item;

public abstract class Instruction implements Comparable<Instruction> {
    private int startDate;
    private int endDate;
    private Item item;

    public Instruction(int startDate, int endDate, Item item) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.item = item;
    }

    public int getStartDate() {
        return startDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public int compareTo(Instruction o) {
        if (startDate < o.startDate)
            return -1;

        if (startDate > o.startDate)
            return 1;

        return item.compareTo(o.item);
    }
}
