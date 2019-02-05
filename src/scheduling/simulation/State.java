package scheduling.simulation;

import scheduling.core.Schedule;
import scheduling.core.input.Demand;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;

import java.util.HashMap;
import java.util.Map;

public class State {
    private int date; // the current date

    private Schedule schedule; // the actual schedule

    private Map<String, Plant> plantMap; // the plants in the system
    private Map<Item, Map<Integer, Demand>> demandMap; // the demand of the items in the system
    private Map<Item, Map<Plant, Integer>> inventoryMap; // the inventory of each item at each plant
    private Map<Item, Map<Integer, Integer>> delayMap; // the delay of each item in each past date
    private double inventoryCost;
    private double productionCost;
    private double transitCost;
    private int totalDelay;

    public State(int startDate) {
        this.date = startDate;
        this.schedule = new Schedule();

        plantMap = new HashMap<>();
        demandMap = new HashMap<>();
        inventoryMap = new HashMap<>();
        delayMap = new HashMap<>();

        inventoryCost = 0d;
        productionCost = 0d;
        transitCost = 0d;
    }

    public int getDate() {
        return date;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public Map<String, Plant> getPlantMap() {
        return plantMap;
    }

    public Map<Item, Map<Integer, Demand>> getDemandMap() {
        return demandMap;
    }

    public Map<Item, Map<Plant, Integer>> getInventoryMap() {
        return inventoryMap;
    }

    public Map<Item, Map<Integer, Integer>> getDelayMap() {
        return delayMap;
    }

    public double getInventoryCost() {
        return inventoryCost;
    }

    public double getProductionCost() {
        return productionCost;
    }

    public double getTransitCost() {
        return transitCost;
    }

    public int getTotalDelay() {
        return totalDelay;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setInventoryMap(Map<Item, Map<Plant, Integer>> inventoryMap) {
        this.inventoryMap = inventoryMap;
    }

    public void setDelayMap(Map<Item, Map<Integer, Integer>> delayMap) {
        this.delayMap = delayMap;
    }

    public void setInventoryCost(double inventoryCost) {
        this.inventoryCost = inventoryCost;
    }

    public void setProductionCost(double productionCost) {
        this.productionCost = productionCost;
    }

    public void setTransitCost(double transitCost) {
        this.transitCost = transitCost;
    }

    public void setTotalDelay(int totalDelay) {
        this.totalDelay = totalDelay;
    }

    public void addInventory(Item item, Plant plant, int quantity) {
        Map<Plant, Integer> itemInvMap = inventoryMap.get(item);

        int oldQuantity = 0;
        if (itemInvMap.containsKey(plant))
            oldQuantity = itemInvMap.get(plant);

        itemInvMap.put(plant, oldQuantity+quantity);
    }

    public void reduceInventory(Item item, Plant plant, int quantity) {
        Map<Plant, Integer> itemInvMap = inventoryMap.get(item);

        int oldQuantity = itemInvMap.get(plant);

        if (oldQuantity == quantity) {
            itemInvMap.remove(plant);
        } else {
            itemInvMap.put(plant, oldQuantity-quantity);
        }
    }
}
