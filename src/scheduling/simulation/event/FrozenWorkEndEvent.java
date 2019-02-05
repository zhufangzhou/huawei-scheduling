package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;

/**
 * The frozen work ends. This applies to the initial work-in-process and frozen productions.
 */
public class FrozenWorkEndEvent extends Event {
    private Item item;
    private Plant plant;
    private int quantity;

    public FrozenWorkEndEvent(int date, Item item, Plant plant, int quantity) {
        super(date);
        this.item = item;
        this.plant = plant;
        this.quantity = quantity;
    }

    @Override
    public void trigger(Simulator simulator) {
        simulator.getState().addInventory(item, plant, quantity);
    }
}
