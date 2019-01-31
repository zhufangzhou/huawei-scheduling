package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;

public class TransitInstruction extends Instruction {
    private int quantity;
    private Plant fromPlant;
    private Plant toPlant;

    public TransitInstruction(int startDate, int endDate, Item item, int quantity, Plant fromPlant, Plant toPlant) {
        super(startDate, endDate, item);
        this.quantity = quantity;
        this.fromPlant = fromPlant;
        this.toPlant = toPlant;
    }

    public int getQuantity() {
        return quantity;
    }

    public Plant getFromPlant() {
        return fromPlant;
    }

    public Plant getToPlant() {
        return toPlant;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(int quantity) {
        this.quantity -= quantity;
    }

    @Override
    public String toString() {
        return "TRAN[" + getItem().toString() + ", " +
                fromPlant.toString() + " -> " + toPlant.toString() + ", " +
                quantity + "]";
    }
}
