package scheduling.core.input;

import org.apache.commons.math3.util.Pair;

import java.util.*;

/**
 * An item.
 */

public class Item implements Comparable<Item> {
    private String id;
    private ItemType type;
    private double materialCost;
    private ProductCategory productCategory;
    private double holdingCost;
    private Map<Pair<Plant, Plant>, Transit> transitMap;
    private Map<CapacityType, Double> rateMap;
    private Map<Plant, Production> productionMap;
    private List<ProportionConstraint> proportionConstraints;
    private Map<Plant, List<MachineSet>> machineMap; // the machine sets in each plant occupied by producing this item
    private Map<Integer, Double> orderDemandMap;
    private Map<Integer, Double> forecastDemandMap;
    private Map<Plant, Double> initInventoryMap;
    private Map<Plant, Map<Integer, Double>> frozenProductionMap;

    private int minProductionLeadTime;
    private Set<Plant> plants; // the plants that can hold the item
    private Set<Item> bomItems; // all the possible items required in its bom
    private Set<MachineSet> sharedMachineSets; // all the machine sets may be occupied to provide the item
    private Set<Item> dependentItems; // all the dependent items

    public Item(String id, ItemType type, double materialCost, ProductCategory productCategory, double holdingCost) {
        this.id = id;
        this.type = type;
        this.materialCost = materialCost;
        this.productCategory = productCategory;
        this.holdingCost = holdingCost;

        transitMap = new HashMap<>();
        rateMap = new HashMap<>();
        productionMap = new HashMap<>();
        proportionConstraints = new LinkedList<>();
        machineMap = new HashMap<>();

        orderDemandMap = new HashMap<>();
        forecastDemandMap = new HashMap<>();
        initInventoryMap = new HashMap<>();
        frozenProductionMap = new HashMap<>();

        plants = new HashSet<>();
        bomItems = new HashSet<>();
        sharedMachineSets = new HashSet<>();
        dependentItems = new HashSet<>();
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

    public Map<CapacityType, Double> getRateMap() {
        return rateMap;
    }

    public void setRateMap(Map<CapacityType, Double> rateMap) {
        this.rateMap = rateMap;
    }

    public Map<Plant, Production> getProductionMap() {
        return productionMap;
    }

    public Map<Plant, List<MachineSet>> getMachineMap() {
        return machineMap;
    }

    public Map<Integer, Double> getOrderDemandMap() {
        return orderDemandMap;
    }

    public Map<Integer, Double> getForecastDemandMap() {
        return forecastDemandMap;
    }

    public Map<Plant, Double> getInitInventoryMap() {
        return initInventoryMap;
    }

    public Map<Plant, Map<Integer, Double>> getFrozenProductionMap() {
        return frozenProductionMap;
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

    public Set<Item> getBomItems() {
        return bomItems;
    }

    public void setBomItems(Set<Item> bomItems) {
        this.bomItems = bomItems;
    }

    public Set<MachineSet> getSharedMachineSets() {
        return sharedMachineSets;
    }

    public void setSharedMachineSets(Set<MachineSet> sharedMachineSets) {
        this.sharedMachineSets = sharedMachineSets;
    }

    public Set<Item> getDependentItems() {
        return dependentItems;
    }

    public void setDependentItems(Set<Item> dependentItems) {
        this.dependentItems = dependentItems;
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
     * Calculate the plants that can hold the item.
     */
    public void calcPlants() {
        // where it is initially
        plants.addAll(initInventoryMap.keySet());

        // where it can be produced
        plants.addAll(productionMap.keySet());
//        plants.addAll(frozenProductionMap.keySet());

        // where it can be transit from/to
        for (Transit transit : transitMap.values()) {
            plants.add(transit.getFromPlant());
            plants.add(transit.getToPlant());
        }
    }

    /**
     * Calculate the bom items.
     */
    public void calcBomItems() {
        for (Production production : productionMap.values()) {
            for (BomComponent component : production.getBom()) {
                Item compItem = component.getMaterial();
                compItem.calcBomItems();

                bomItems.addAll(compItem.getBomItems());
                bomItems.add(compItem);
            }
        }
    }

    /**
     * Calculate the shared machine sets.
     */
    public void calcSharedMachineSets() {
        for (List<MachineSet> machineSets : machineMap.values()) {
            for (MachineSet machineSet : machineSets) {
                if (rateMap.get(machineSet.getCapacityType()) > 0) {
                    sharedMachineSets.add(machineSet);
                }
            }
        }

        for (Production production : productionMap.values()) {
            for (BomComponent component : production.getBom()) {
                Item compItem = component.getMaterial();
                compItem.calcSharedMachineSets();

                sharedMachineSets.addAll(compItem.getSharedMachineSets());
            }
        }
    }

    public void addCapacity(Plant plant, int dateId, double quantity) {
        List<MachineSet> machineSets = machineMap.get(plant);

        for (MachineSet machineSet : machineSets) {
            double rate = rateMap.get(machineSet.getCapacityType());

            machineSet.getCapacityMap().get(dateId).addRemaining(rate * quantity);
        }
    }

    public void reduceCapacity(Plant plant, int dateId, double quantity) {
        List<MachineSet> machineSets = machineMap.get(plant);

        for (MachineSet machineSet : machineSets) {
            double rate = rateMap.get(machineSet.getCapacityType());

            if (rate == 0)
                continue;

//            System.out.println(machineSet.toString() + ", " + dateId);
            machineSet.getCapacityMap().get(dateId).reduceRemaining(rate*quantity);
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
