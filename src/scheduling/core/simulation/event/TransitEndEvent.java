package scheduling.core.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.output.TransitInstruction;
import scheduling.core.simulation.Event;
import scheduling.core.simulation.Simulator;
import scheduling.core.simulation.State;

import java.util.Map;

public class TransitEndEvent extends Event {
    private TransitInstruction instruction;

    public TransitEndEvent(int date, TransitInstruction instruction) {
        super(date);
        this.instruction = instruction;
    }

    public TransitInstruction getInstruction() {
        return instruction;
    }

    /**
     * Ends a transit.
     * (1) Increase the inventory of the item at the target plant.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        Item item = instruction.getItem();
        Plant plant = instruction.getToPlant();
        int quantity = instruction.getQuantity();

        Map<Plant, Integer> map = simulator.getState().getInventoryMap().get(item);

        int old = 0;
        if (map.containsKey(plant))
            old = map.get(plant);

        map.put(plant, old+quantity);
    }

    @Override
    public String toString() {
        return "<TranStart: " + instruction.toString() + ">";
    }
}
