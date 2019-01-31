package scheduling.core.simulation.event;

import scheduling.core.input.Capacity;
import scheduling.core.input.MachineSet;
import scheduling.core.input.TimePeriod;
import scheduling.core.simulation.Event;
import scheduling.core.simulation.Simulator;

import java.util.Map;

public class CapacityUpdateEvent extends Event {
    private MachineSet machineSet;
    private Map<TimePeriod, Capacity> capacityMap;

    public CapacityUpdateEvent(int date, MachineSet machineSet, Map<TimePeriod, Capacity> capacityMap) {
        super(date);
        this.machineSet = machineSet;
        this.capacityMap = capacityMap;
    }

    /**
     * update the capacity of the machine set, when a new day comes.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        machineSet.setCapacityMap(capacityMap);
    }

    @Override
    public String toString() {
        return "<CapUpdate: " + machineSet.toString() + ">";
    }
}
