package scheduling.core.input;

import java.util.HashMap;
import java.util.Map;

/**
 * A machine set.
 */

public class MachineSet implements Comparable<MachineSet> {
    private String name;
    private Plant plant;
    private CapacityType capacityType;
    private double smoothingFactor;
    private Map<Integer, Double> capacityMap;

    public MachineSet(String name, Plant plant, CapacityType capacityType, double smoothingFactor) {
        this.name = name;
        this.plant = plant;
        this.capacityType = capacityType;
        this.smoothingFactor = smoothingFactor;

        capacityMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Plant getPlant() {
        return plant;
    }

    public CapacityType getCapacityType() {
        return capacityType;
    }

    public double getSmoothingFactor() {
        return smoothingFactor;
    }

    public Map<Integer, Double> getCapacityMap() {
        return capacityMap;
    }

    public void putCapacity(Integer date, double cap) {
        capacityMap.put(date, cap);
    }

    public void setCapacityMap(Map<Integer, Double> capacityMap) {
        this.capacityMap = capacityMap;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(MachineSet o) {
        return name.compareTo(o.name);
    }
}
