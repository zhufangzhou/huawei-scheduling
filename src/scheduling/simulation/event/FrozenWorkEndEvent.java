package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

/**
 * The frozen work ends. This applies to the initial work-in-process and frozen productions.
 */
public class FrozenWorkEndEvent extends Event {
    private Item item;
    private Plant plant;
    private long quantity;

    public FrozenWorkEndEvent(int dateId, Item item, Plant plant, long quantity) {
        super(dateId);
        this.item = item;
        this.plant = plant;
        this.quantity = quantity;
    }

    @Override
    public void trigger(State state) {
        state.getExecutedSchedule().addInventory(dateId, item, plant, quantity);
    }
}
