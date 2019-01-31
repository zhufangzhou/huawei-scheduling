package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.output.SupplyInstruction;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.Map;

public class SupplyEvent extends Event {
    private SupplyInstruction instruction;

    public SupplyEvent(int date, SupplyInstruction instruction) {
        super(date);
        this.instruction = instruction;
    }

    public SupplyInstruction getInstruction() {
        return instruction;
    }

    /**
     * Supply happens.
     * (1) Reduce the delay amount of supplied item at that day.
     * (2) Reduce the inventory of the supplied item at the plant.
     * (3) Add the supply instruction into the schedule.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        Item item = instruction.getItem();
        Plant plant = instruction.getPlant();
        int quantity = instruction.getQuantity();

        State state = simulator.getState();
        Map<Integer, Integer> itemDelayMap = state.getDelayMap().get(item);
        int oldDelay = itemDelayMap.get(date);
        itemDelayMap.put(date, oldDelay-quantity);

        Map<Plant, Integer> inventory = state.getInventoryMap().get(item);
        int oldInv = inventory.get(plant);
        inventory.put(plant, oldInv-quantity);

        state.getSchedule().getSupplySchedule().add(instruction);
    }

    @Override
    public String toString() {
        return "<Supply: " + instruction.toString() + ">";
    }
}
