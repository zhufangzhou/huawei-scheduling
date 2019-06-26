package scheduling.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.output.TransitInstruction;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.Map;

public class TransitEndEvent extends Event {
    private TransitInstruction instruction;

    public TransitEndEvent(int dateId, TransitInstruction instruction) {
        super(dateId);
        this.instruction = instruction;
    }

    public TransitInstruction getInstruction() {
        return instruction;
    }

    /**
     * Ends a transit.
     * (1) Increase the inventory of the item at the target plant.
     * @param state the state.
     */
    @Override
    public void trigger(State state) {
        Item item = instruction.getItem();
        Plant plant = instruction.getTransit().getToPlant();
        double quantity = instruction.getQuantity();

//        state.addInventory(item, plant, quantity);
    }

    @Override
    public String toString() {
        return "<TranStart: " + instruction.toString() + ">";
    }
}
