package scheduling.core;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.output.*;
import scheduling.simulation.State;

import java.util.*;

/**
 * A schedule contains a set of
 *  production instructions,
 *  transit instructions,
 *  supply instructions.
 *
 * Each instruction has a start and end date.
 *
 */
public class Schedule {
    private List<ProductionInstruction> productionSchedule;
    private List<TransitInstruction> transitSchedule;
    private List<SupplyInstruction> supplySchedule;

    private Map<Integer, Map<Item, Map<Plant, Long>>> inventoryMap;
    private Map<Integer, Map<Item, Long>> delayMap;

    private double inventoryCost;
    private double productionCost;
    private double transitCost;
    private long totalDelay;

    /**
     * Construct an empty schedule.
     */
    public Schedule() {
        productionSchedule = new ArrayList<>();
        transitSchedule = new ArrayList<>();
        supplySchedule = new ArrayList<>();

        inventoryMap = new HashMap<>();
        delayMap = new HashMap<>();

        inventoryCost = 0d;
        productionCost = 0d;
        transitCost = 0d;
        totalDelay = 0;
    }

    public List<ProductionInstruction> getProductionSchedule() {
        return productionSchedule;
    }

    public void setProductionSchedule(List<ProductionInstruction> productionSchedule) {
        this.productionSchedule = productionSchedule;
    }

    public List<TransitInstruction> getTransitSchedule() {
        return transitSchedule;
    }

    public void setTransitSchedule(List<TransitInstruction> transitSchedule) {
        this.transitSchedule = transitSchedule;
    }

    public List<SupplyInstruction> getSupplySchedule() {
        return supplySchedule;
    }

    public void setSupplySchedule(List<SupplyInstruction> supplySchedule) {
        this.supplySchedule = supplySchedule;
    }

    public Map<Integer, Map<Item, Map<Plant, Long>>> getInventoryMap() {
        return inventoryMap;
    }

    public void setInventoryMap(Map<Integer, Map<Item, Map<Plant, Long>>> inventoryMap) {
        this.inventoryMap = inventoryMap;
    }

    public Map<Integer, Map<Item, Long>> getDelayMap() {
        return delayMap;
    }

    public void setDelayMap(Map<Integer, Map<Item, Long>> delayMap) {
        this.delayMap = delayMap;
    }

    public double getInventoryCost() {
        return inventoryCost;
    }

    public void setInventoryCost(double inventoryCost) {
        this.inventoryCost = inventoryCost;
    }

    public double getProductionCost() {
        return productionCost;
    }

    public void setProductionCost(double productionCost) {
        this.productionCost = productionCost;
    }

    public double getTransitCost() {
        return transitCost;
    }

    public void setTransitCost(double transitCost) {
        this.transitCost = transitCost;
    }

    public long getTotalDelay() {
        return totalDelay;
    }

    public void setTotalDelay(long totalDelay) {
        this.totalDelay = totalDelay;
    }

    public void addInventory(int dateId, Item item, Plant plant, long quantity) {
        Map<Plant, Long> itemInvMap = inventoryMap.get(dateId).get(item);

        long oldQuantity = 0;
        if (itemInvMap.containsKey(plant))
            oldQuantity = itemInvMap.get(plant);

        itemInvMap.put(plant, oldQuantity+quantity);
    }

    public void removeInventory(int dateId, Item item, Plant plant, int quantity) {
        Map<Plant, Long> itemInvMap = inventoryMap.get(dateId).get(item);

        long oldQuantity = itemInvMap.get(plant);

        if (oldQuantity == quantity) {
            itemInvMap.remove(plant);
        } else {
            itemInvMap.put(plant, oldQuantity-quantity);
        }
    }
}
