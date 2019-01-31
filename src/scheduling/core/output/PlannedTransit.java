package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;

public class PlannedTransit {
    private Item item;
    private int quantity;
    private Plant fromPlant;
    private Plant toPlant;
    private int startDate;
    private int finishDate;

    public PlannedTransit(Item item, int quantity, Plant fromPlant, Plant toPlant, int startDate, int finishDate) {
        this.item = item;
        this.quantity = quantity;
        this.fromPlant = fromPlant;
        this.toPlant = toPlant;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public Item getItem() {
        return item;
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

    public int getStartDate() {
        return startDate;
    }

    public int getFinishDate() {
        return finishDate;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(int quantity) {
        this.quantity -= quantity;
    }
}
