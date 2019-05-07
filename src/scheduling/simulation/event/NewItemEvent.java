package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.HashMap;

public class NewItemEvent extends Event {
    private Item item;

    public NewItemEvent(int dateId, Item item) {
        super(dateId);
        this.item = item;
    }

    /**
     * A new item comes into the system.
     * @param state the state.
     */
    @Override
    public void trigger(State state) {

    }

    @Override
    public String toString() {
        return "<NewItem: " + item.toString() + ">";
    }
}
