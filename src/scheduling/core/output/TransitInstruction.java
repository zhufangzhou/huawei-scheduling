package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.input.Transit;

public class TransitInstruction extends Instruction {
    private Transit transit;
    private long quantity;

    public TransitInstruction(int startDate, int endDate, Item item, Transit transit, long quantity) {
        super(startDate, endDate, item);
        this.transit = transit;
        this.quantity = quantity;
    }

    public Transit getTransit() {
        return transit;
    }

    public void setTransit(Transit transit) {
        this.transit = transit;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "[" + transit.toString() + ": " + quantity + "]";
    }
}
