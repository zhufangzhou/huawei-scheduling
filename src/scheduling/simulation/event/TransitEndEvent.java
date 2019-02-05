package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.output.TransitInstruction;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;

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

        simulator.getState().addInventory(item, plant, quantity);
    }

    @Override
    public String toString() {
        return "<TranStart: " + instruction.toString() + ">";
    }
}
