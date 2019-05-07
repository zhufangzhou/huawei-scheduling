package scheduling.simulation.event;

import scheduling.core.input.*;
import scheduling.core.output.ProductionInstruction;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.Map;

public class ProductionEndEvent extends Event {
    private ProductionInstruction instruction;

    public ProductionEndEvent(int dateId, ProductionInstruction instruction) {
        super(dateId);
        this.instruction = instruction;
    }

    public ProductionInstruction getInstruction() {
        return instruction;
    }

    /**
     * End a production.
     * (1) Increase the inventory of the produced item.
     * @param state the state.
     */
    @Override
    public void trigger(State state) {
        Production production = instruction.getProduction();
        Item item = production.getItem();
        Plant plant = production.getPlant();
        long quantity = instruction.getQuantity();

//        simulator.getState().addInventory(item, plant, quantity);
    }

    @Override
    public String toString() {
        return "<ProdEnd: " + instruction.toString() + ">";
    }
}
