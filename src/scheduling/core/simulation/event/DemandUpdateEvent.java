package scheduling.core.simulation.event;

import scheduling.core.input.Demand;
import scheduling.core.input.Item;
import scheduling.core.simulation.Event;
import scheduling.core.simulation.Simulator;

import java.util.HashMap;
import java.util.Map;

public class DemandUpdateEvent extends Event {
    private Item item;
    private int dueDate;
    private Demand demand;

    public DemandUpdateEvent(int date, Item item, int dueDate, Demand demand) {
        super(date);
        this.item = item;
        this.dueDate = dueDate;
        this.demand = demand;
    }

    /**
     * Update the demand of an item, whenever a new day comes.
     * [item, [due date, demand]].
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        Map<Item, Map<Integer, Demand>> demandMap = simulator.getState().getDemandMap();

        Map<Integer, Demand> itemDemandMap = demandMap.get(item);

        if (itemDemandMap == null) {
            itemDemandMap = new HashMap<>();
            demandMap.put(item, itemDemandMap);
        }

        itemDemandMap.put(dueDate, demand);
    }

    @Override
    public String toString() {
        return "<DemUpdate: " + item.toString() + ", " + dueDate + ", " + demand.toString() + ">";
    }
}
