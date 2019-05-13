package scheduling.core.input;

import org.apache.commons.math3.util.Pair;
import scheduling.simulation.Candidate;

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
    private Map<Pair<Plant, Plant>, Transit> transitMap;
    private CapacityType capacityType;
    private double rate;
    private Map<Plant, Production> productionMap;
    private List<ProportionConstraint> proportionConstraints;
    private Map<Plant, MachineSet> machineMap;
    private Map<Integer, Long> orderDemandMap;
    private Map<Integer, Long> forecastDemandMap;
    private Map<Plant, Long> initInventoryMap;
    private Map<Plant, Map<Integer, Long>> frozenProductionMap;

    private int minProductionLeadTime;
    private Set<Plant> plants; // the plants that can hold the item

    public Item(String id, ItemType type, double materialCost, ProductCategory productCategory, double holdingCost) {
        this.id = id;
        this.type = type;
        this.materialCost = materialCost;
        this.productCategory = productCategory;
        this.holdingCost = holdingCost;

        transitMap = new HashMap<>();
        productionMap = new HashMap<>();
        proportionConstraints = new LinkedList<>();
        machineMap = new HashMap<>();

        orderDemandMap = new HashMap<>();
        forecastDemandMap = new HashMap<>();
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

    public Map<Pair<Plant, Plant>, Transit> getTransitMap() {
        return transitMap;
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

    public Map<Integer, Long> getOrderDemandMap() {
        return orderDemandMap;
    }

    public Map<Integer, Long> getForecastDemandMap() {
        return forecastDemandMap;
    }

    public Map<Plant, Long> getInitInventoryMap() {
        return initInventoryMap;
    }

    public Map<Plant, Map<Integer, Long>> getFrozenProductionMap() {
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

    public int getMinProductionLeadTime() {
        return minProductionLeadTime;
    }

    public void setMinProductionLeadTime(int minProductionLeadTime) {
        this.minProductionLeadTime = minProductionLeadTime;
    }

    public Set<Plant> getPlants() {
        return plants;
    }

    public void setPlants(Set<Plant> plants) {
        this.plants = plants;
    }

    public void addProportionConstraint(ProportionConstraint proportionConstraint) {
        proportionConstraints.add(proportionConstraint);
    }

    public void addTransit(Transit transit) {
        Plant fromPlant = transit.getFromPlant();
        Plant toPlant = transit.getToPlant();
        transitMap.put(new Pair<>(fromPlant, toPlant), transit);
    }

    public void calcMinProdLeadTime() {
        minProductionLeadTime = Integer.MAX_VALUE;
        for (Production production : productionMap.values()) {
            int leadTime = production.getLeadTime();
            if (minProductionLeadTime > leadTime)
                minProductionLeadTime = leadTime;
        }
    }

    /**
     * Calculate the plants that can hold the item
     */
    public void calcPlants() {
        plants = new HashSet<>();

        // where it is initially
        plants.addAll(initInventoryMap.keySet());

        // where it can be produced
        plants.addAll(productionMap.keySet());
        plants.addAll(frozenProductionMap.keySet());

        // where it can be transit from/to
        for (Transit transit : transitMap.values()) {
            plants.add(transit.getFromPlant());
            plants.add(transit.getToPlant());
        }
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
