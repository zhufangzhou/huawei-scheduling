package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;

public class PlannedSupply {
    private Item item;
    private Plant plant;
    private int quantity;

    public PlannedSupply(Item item, Plant plant, int quantity) {
        this.item = item;
        this.plant = plant;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
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
}
