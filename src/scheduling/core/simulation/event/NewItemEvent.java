package scheduling.core.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.simulation.Event;
import scheduling.core.simulation.Simulator;

import java.util.HashMap;

public class NewItemEvent extends Event {
    private Item item;

    public NewItemEvent(int date, Item item) {
        super(date);
        this.item = item;
    }

    /**
     * A new item comes into the system.
     * Set its initial inventory.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        simulator.getState().getInventoryMap().put(item, new HashMap<>(item.getInitInventoryMap()));
    }

    @Override
    public String toString() {
        return "<NewItem: " + item.toString() + ">";
    }
}
