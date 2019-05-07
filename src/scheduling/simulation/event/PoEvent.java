package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.HashMap;
import java.util.Map;

public class PoEvent extends Event {
    private Item item;
    private Plant plant;
    private long quantity;

    public PoEvent(int dateId, Item item, Plant plant, long quantity) {
        super(dateId);
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

    public long getQuantity() {
        return quantity;
    }

    /**
     * Buy some items at a plant.
     * (1) Increase the inventory of the item at the plant.
     * @param state the state.
     */
    @Override
    public void trigger(State state) {

    }

    @Override
    public String toString() {
        return "<Po: " + item.toString() + ", " + plant.toString() + ", " + quantity + ">";
    }
}
