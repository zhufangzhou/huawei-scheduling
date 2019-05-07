package scheduling.simulation.event;

import scheduling.core.input.Capacity;
import scheduling.core.input.MachineSet;
import scheduling.core.input.TimePeriod;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.Map;

/**
 * The event of updating the capacity of a machine set.
 */

public class CapacityUpdateEvent extends Event {
    private MachineSet machineSet;
    private Map<Integer, Double> capacityMap;

    public CapacityUpdateEvent(int dateId, MachineSet machineSet, Map<Integer, Double> capacityMap) {
        super(dateId);
        this.machineSet = machineSet;
        this.capacityMap = capacityMap;
    }

    /**
     * update the capacity of the machine set, when a new day comes.
     * @param state the state.
     */
    @Override
    public void trigger(State state) {
        machineSet.setCapacityMap(capacityMap);
    }

    @Override
    public String toString() {
        return "<CapUpdate: " + machineSet.toString() + ">";
    }
}
