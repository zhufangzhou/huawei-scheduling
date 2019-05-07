package scheduling.simulation.event;

import scheduling.core.input.Demand;
import scheduling.core.input.Item;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.HashMap;
import java.util.Map;

public class OrderDemandUpdateEvent extends Event {
    private Item item;
    private int dueDateId;
    private long demand;

    public OrderDemandUpdateEvent(int dateId, Item item, int dueDateId, long demand) {
        super(dateId);
        this.item = item;
        this.dueDateId = dueDateId;
        this.demand = demand;
    }

    /**
     * Update the demand of an item, whenever a new day comes.
     * [item, [due date, demand]].
     * @param state the state.
     */
    @Override
    public void trigger(State state) {

    }

    @Override
    public String toString() {
        return "<DemUpdate: " + item.toString() + ", " + dueDateId + ", " + demand + ">";
    }
}
