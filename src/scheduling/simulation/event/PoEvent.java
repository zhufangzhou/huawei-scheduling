package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;

import java.util.HashMap;
import java.util.Map;

public class PoEvent extends Event {
    private Item item;
    private Plant plant;
    private int quantity;

    public PoEvent(int date, Item item, Plant plant, int quantity) {
        super(date);
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

    /**
     * Buy some items at a plant.
     * (1) Increase the inventory of the item at the plant.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        simulator.getState().addInventory(item, plant, quantity);
    }

    @Override
    public String toString() {
        return "<Po: " + item.toString() + ", " + plant.toString() + ", " + quantity + ">";
    }
}
