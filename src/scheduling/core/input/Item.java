package scheduling.core.input;

import org.apache.commons.math3.util.Pair;
import scheduling.core.simulation.Candidate;

import java.util.*;

/**
 * An item.
 */

public class Item extends Candidate implements Comparable<Item> {
    private String id;
    private ItemType type;
    private double materialCost;
    private ProductCategory productCategory;
    private double holdingCost;
    private List<Pair<Plant, Plant>> transits;
    private CapacityType capacityType;
    private double rate;
    private Map<Plant, Production> productionMap;
    private List<ProportionConstraint> proportionConstraints;
    private Map<Plant, MachineSet> machineMap;

    private Map<TimePeriod, Demand> demandMap;
    private Map<Plant, Integer> initInventoryMap;
    private Map<Plant, Map<TimePeriod, Integer>> frozenProductionMap;

    public Item(String id, ItemType type, double materialCost, ProductCategory productCategory, double holdingCost) {
        this.id = id;
        this.type = type;
        this.materialCost = materialCost;
        this.productCategory = productCategory;
        this.holdingCost = holdingCost;

        transits = new ArrayList<>();
        productionMap = new HashMap<>();
        proportionConstraints = new LinkedList<>();
        machineMap = new HashMap<>();

        demandMap = new HashMap<>();
        initInventoryMap = new HashMap<>();
        frozenProductionMap = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public ItemType getType() {
        return type;
    }

    public double getMaterialCost() {
        return materialCost;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public double getHoldingCost() {
        return holdingCost;
    }

    public List<Pair<Plant, Plant>> getTransits() {
        return transits;
    }

    public CapacityType getCapacityType() {
        return capacityType;
    }

    public double getRate() {
        return rate;
    }

    public Map<Plant, Production> getProductionMap() {
        return productionMap;
    }

    public Map<Plant, MachineSet> getMachineMap() {
        return machineMap;
    }

    public Map<TimePeriod, Demand> getDemandMap() {
        return demandMap;
    }

    public Map<Plant, Integer> getInitInventoryMap() {
        return initInventoryMap;
    }

    public Map<Plant, Map<TimePeriod, Integer>> getFrozenProductionMap() {
        return frozenProductionMap;
    }

    public void setCapacityType(CapacityType capacityType) {
        this.capacityType = capacityType;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public List<ProportionConstraint> getProportionConstraints() {
        return proportionConstraints;
    }

    public void putProduction(Production production) {
        productionMap.put(production.getPlant(), production);
    }

    public Production getProduction(Plant plant) {
        return productionMap.get(plant);
    }

    public void addProportionConstraint(ProportionConstraint proportionConstraint) {
        proportionConstraints.add(proportionConstraint);
    }

    public void addTransit(Plant fromPlant, Plant toPlant) {
        transits.add(new Pair<>(fromPlant, toPlant));
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(Item o) {
        return id.compareTo(o.id);
    }
}
