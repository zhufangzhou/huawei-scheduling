package scheduling.core.simulation.event;

import scheduling.core.input.*;
import scheduling.core.output.ProductionInstruction;
import scheduling.core.simulation.Event;
import scheduling.core.simulation.Simulator;
import scheduling.core.simulation.State;

import java.util.Map;

public class ProductionStartEvent extends Event {
    private ProductionInstruction instruction;

    public ProductionStartEvent(int date, ProductionInstruction instruction) {
        super(date);
        this.instruction = instruction;
    }

    public ProductionInstruction getInstruction() {
        return instruction;
    }

    /**
     * Start a production.
     * (1) Reduce the inventory of the BOM of the produced item.
     * (2) Occupy the capacity of the machine set.
     * (3) Add a production cost.
     * (4) Add the production instruction into the schedule.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        Production production = instruction.getProduction();
        Item item = production.getItem();
        Plant plant = production.getPlant();
        MachineSet machineSet = item.getMachineMap().get(plant);
        int quantity = instruction.getQuantity();

        State state = simulator.getState();
        int date = state.getDate();

        Map<Item, Map<Plant, Integer>> inventoryMap = state.getInventoryMap();

        for (Bom bom : production.getAssembly()) {
            Item component = bom.getComponent();
            int n = bom.getQuantity();

            Map<Plant, Integer> map = inventoryMap.get(component);
            int oldInv = map.get(plant);
            map.put(plant, oldInv-n*quantity);
        }

        Capacity capacity = machineSet.getCapacityMap().get(date);
        double oldCap = capacity.getRemaining();
        capacity.setRemaining(oldCap-item.getRate()*quantity);

        double oldCost = state.getProductionCost();
        int duration = instruction.getEndDate()-instruction.getStartDate()+1;
        double addedCost = production.getCost() * quantity * duration;
        state.setProductionCost(oldCost+addedCost);

        state.getSchedule().getProductionSchedule().add(instruction);
    }

    @Override
    public String toString() {
        return "<ProdStart: " + instruction.toString() + ">";
    }
}
