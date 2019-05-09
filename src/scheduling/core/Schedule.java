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
    private State state; // the state/problem
    private int startDateId; // the start date id of the schedule
    private int endDateId; // the end date id of the schedule

    private List<ProductionInstruction> productionSchedule;
    private List<TransitInstruction> transitSchedule;
    private List<SupplyInstruction> supplySchedule;

    private Map<Integer, Map<Item, Map<Plant, Long>>> inventoryMap;
    private Map<Integer, Map<Item, Long>> orderSupplyMap;
    private Map<Integer, Map<Item, Long>> forecastSupplyMap;
    private Map<Integer, Map<Item, Long>> accOrderDemMap; // accumulated order demand each day

    private double inventoryCost;
    private double productionCost;
    private double transitCost;
    private long totalDelay;

    /**
     * Construct an empty schedule without any other information.
     */
    public Schedule() {
        productionSchedule = new ArrayList<>();
        transitSchedule = new ArrayList<>();
        supplySchedule = new ArrayList<>();

        inventoryMap = new HashMap<>();
        orderSupplyMap = new HashMap<>();
        forecastSupplyMap = new HashMap<>();
        accOrderDemMap = new HashMap<>();

        inventoryCost = 0d;
        productionCost = 0d;
        transitCost = 0d;
        totalDelay = 0l;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getStartDateId() {
        return startDateId;
    }

    public void setStartDateId(int startDateId) {
        this.startDateId = startDateId;
    }

    public int getEndDateId() {
        return endDateId;
    }

    public void setEndDateId(int endDateId) {
        this.endDateId = endDateId;
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

    public Map<Integer, Map<Item, Long>> getOrderSupplyMap() {
        return orderSupplyMap;
    }

    public void setOrderSupplyMap(Map<Integer, Map<Item, Long>> orderSupplyMap) {
        this.orderSupplyMap = orderSupplyMap;
    }

    public Map<Integer, Map<Item, Long>> getForecastSupplyMap() {
        return forecastSupplyMap;
    }

    public void setForecastSupplyMap(Map<Integer, Map<Item, Long>> forecastSupplyMap) {
        this.forecastSupplyMap = forecastSupplyMap;
    }

    public Map<Integer, Map<Item, Long>> getAccOrderDemMap() {
        return accOrderDemMap;
    }

    public void setAccOrderDemMap(Map<Integer, Map<Item, Long>> accOrderDemMap) {
        this.accOrderDemMap = accOrderDemMap;
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

    /**
     * Initialise the fields of an empty schedule with the current state/problem.
     * @param state the current state.
     */
    public void initWithState(State state) {
        this.state = state;
        this.startDateId = state.getDateIndex();
        this.endDateId = state.getEnv().getPeriod()-1;

        // set the inventory in the start date based on the initial inventories
        Map<Item, Map<Plant, Long>> initialInventoryMap = new HashMap<>();
        for (Item item : state.getEnv().getItemMap().values()) {
            Map<Plant, Long> map = new HashMap<>();
            for (Plant plant : item.getInitInventoryMap().keySet()) {
                long quantity = item.getInitInventoryMap().get(plant);

                if (quantity > 0)
                    map.put(plant, quantity);
            }
            initialInventoryMap.put(item, map);
        }

        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            inventoryMap.put(dateId, new HashMap<>(initialInventoryMap));
        }

        // initialise the accumulated order demand map
        // the accumulated order demand adds up all the leftover order demands
        Map<Item, Long> leftover = new HashMap<>();
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            Map<Item, Long> dailyAccOrderDem = new HashMap<>(leftover);

            List<Item> dailyOrderDem = state.getOrderDemMap().get(dateId);

            if (dailyOrderDem != null) {
                for (Item item : dailyOrderDem) {
                    if (dailyAccOrderDem.containsKey(item)) {
                        long old = dailyAccOrderDem.get(item);
                        dailyAccOrderDem.put(item, old+item.getOrderDemandMap().get(dateId));
                    } else {
                        dailyAccOrderDem.put(item, item.getOrderDemandMap().get(dateId));
                    }
                }
            }

            leftover = dailyAccOrderDem;

            accOrderDemMap.put(dateId, dailyAccOrderDem);
        }

        // calculate the inventory cost by summing up for all the days
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            for (Item item : inventoryMap.get(dateId).keySet()) {
                long totalInventory = 0;

                for (Plant plant : inventoryMap.get(dateId).get(item).keySet()) {
                    totalInventory += inventoryMap.get(dateId).get(item).get(plant);
                }

                inventoryCost += totalInventory * item.getHoldingCost();
            }
        }

        // calculate the total delay based on accumulated delay map
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            for (Item item : accOrderDemMap.get(dateId).keySet()) {
                totalDelay += accOrderDemMap.get(dateId).get(item);
            }
        }

        for (int dateId : state.getForecastDemMap().keySet()) {
            for (Item item : state.getForecastDemMap().get(dateId)) {
                totalDelay += item.getForecastDemandMap().get(dateId);
            }
        }
    }
}
