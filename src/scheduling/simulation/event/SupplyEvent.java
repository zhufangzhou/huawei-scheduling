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
     * @param state the state.
     */
    @Override
    public void trigger(State state) {
        Item item = instruction.getItem();
        Plant plant = instruction.getSupply().getSecond();
        long quantity = instruction.getQuantity();

//        Map<Integer, Integer> itemDelayMap = state.getDelayMap().get(item);
//        int oldDelay = itemDelayMap.get(date);
//        itemDelayMap.put(date, oldDelay-quantity);
//
//        state.reduceInventory(item, plant, quantity);
//
//        state.getSchedule().getSupplySchedule().add(instruction);
    }

    @Override
    public String toString() {
        return "<Supply: " + instruction.toString() + ">";
    }
}
