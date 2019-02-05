package scheduling.simulation.event;

import javafx.util.Pair;
import scheduling.core.input.*;
import scheduling.core.output.TransitInstruction;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.Map;

public class TransitStartEvent extends Event {
    private TransitInstruction instruction;

    public TransitStartEvent(int date, TransitInstruction instruction) {
        super(date);
        this.instruction = instruction;
    }

    public TransitInstruction getInstruction() {
        return instruction;
    }

    /**
     * Start a transit.
     * (1) Reduce the item's inventory at the source plant.
     * (2) Add a transit cost.
     * (3) Add the transit instruction into the schedule.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        Item item = instruction.getItem();
        Plant fromPlant = instruction.getFromPlant();
        Plant toPlant = instruction.getToPlant();
        int quantity = instruction.getQuantity();

        State state = simulator.getState();

        state.reduceInventory(item, fromPlant, quantity);

        double oldCost = state.getTransitCost();
        Pair<Plant, Plant> plantPair = new Pair<>(fromPlant, toPlant);
        int unitTransitCost = simulator.getEnvironment().getTransitCostMap().get(plantPair);
        int duration = instruction.getEndDate()-instruction.getStartDate()+1;
        double addedCost = unitTransitCost * quantity * duration;
        state.setTransitCost(oldCost+addedCost);

        state.getSchedule().getTransitSchedule().add(instruction);
    }

    @Override
    public String toString() {
        return "<TranEnd: " + instruction.toString() + ">";
    }
}
