package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.input.Transit;

public class TransitInstruction extends Instruction {
    private Transit transit;
    private double quantity;

    public TransitInstruction(int startDate, int endDate, Item item, Transit transit, double quantity) {
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "[" + transit.toString() + ": " + quantity + "]";
    }
}
