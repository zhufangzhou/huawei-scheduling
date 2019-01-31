package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;

public class SupplyInstruction extends Instruction {
    private Plant plant;
    private int quantity;

    public SupplyInstruction(int startDate, int endDate, Item item, Plant plant, int quantity) {
        super(startDate, endDate, item);
        this.plant = plant;
        this.quantity = quantity;
    }

    public Plant getPlant() {
        return plant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(int quantity) {
        this.quantity -= quantity;
    }

    @Override
    public String toString() {
        return "SUPP[" + getItem().toString() + ", " + plant.toString() +
                ", " + quantity + "]";
    }
}
