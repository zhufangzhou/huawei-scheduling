package scheduling.simulation.event;

//import javafx.util.Pair;
import scheduling.core.input.*;
import scheduling.core.output.TransitInstruction;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;
import scheduling.simulation.State;

import java.util.Map;

public class TransitStartEvent extends Event {
    private TransitInstruction instruction;

    public TransitStartEvent(int dateId, TransitInstruction instruction) {
        super(dateId);
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
     * @param state the state.
     */
    @Override
    public void trigger(State state) {
        Item item = instruction.getItem();
        Plant fromPlant = instruction.getTransit().getFromPlant();
        Plant toPlant = instruction.getTransit().getToPlant();
        long quantity = instruction.getQuantity();

//        state.reduceInventory(item, fromPlant, quantity);
//
//        double oldCost = state.getTransitCost();
//        Pair<Plant, Plant> plantPair = new Pair<>(fromPlant, toPlant);
//        int unitTransitCost = simulator.getEnvironment().getTransitCostMap().get(plantPair);
//        int duration = instruction.getEndDate()-instruction.getStartDate()+1;
//        double addedCost = unitTransitCost * quantity * duration;
//        state.setTransitCost(oldCost+addedCost);
//
//        state.getSchedule().getTransitSchedule().add(instruction);
    }

    @Override
    public String toString() {
        return "<TranEnd: " + instruction.toString() + ">";
    }
}
