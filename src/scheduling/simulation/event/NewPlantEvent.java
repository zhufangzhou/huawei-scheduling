package scheduling.simulation.event;

import scheduling.core.input.Plant;
import scheduling.simulation.Event;
import scheduling.simulation.Simulator;

public class NewPlantEvent extends Event {
    private Plant plant;

    public NewPlantEvent(int date, Plant plant) {
        super(date);
        this.plant = plant;
    }

    /**
     * Open a new plant, and add the plant into the state.
     *
     * @param simulator the simulator
     */
    @Override
    public void trigger(Simulator simulator) {
        simulator.getState().getPlantMap().put(plant.getName(), plant);
    }

    @Override
    public String toString() {
        return "<NewPlant: " + plant.toString() + ">";
    }
}
