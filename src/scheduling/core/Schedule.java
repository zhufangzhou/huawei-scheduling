package scheduling.core;

import org.apache.commons.math3.util.Pair;
import scheduling.core.input.*;
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
    private int startDateId; // the start date id of the schedule, inclusive
    private int endDateId; // the end date id of the schedule, exclusive

    private Map<Integer, Map<Production, ProductionInstruction>> productionSchedule;
    private Map<Integer, Map<Transit, TransitInstruction>> transitSchedule;
    private Map<Integer, Map<Pair<Item, Plant>, SupplyInstruction>> supplySchedule;

    private Map<Integer, Map<Item, Map<Plant, Inventory>>> inventoryMap; // the inventory of each item at each plant in each day
    private Map<Integer, Map<Item, Long>> orderSupplyMap; // needed to calculate delay efficiently
    private Map<Integer, Map<Item, Long>> forecastSupplyMap; // need to calculate delay efficiently
    private Map<Integer, Map<Item, Long>> accOrderDemMap; // accumulated order demand each day

    private double inventoryCost;
    private double productionCost;
    private double transitCost;
    private long totalDelay;

    /**
     * Construct an empty schedule without any other information.
     */
    public Schedule() {
        productionSchedule = new HashMap<>();
        transitSchedule = new HashMap<>();
        supplySchedule = new HashMap<>();

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

    public Map<Integer, Map<Production, ProductionInstruction>> getProductionSchedule() {
        return productionSchedule;
    }

    public void setProductionSchedule(Map<Integer, Map<Production, ProductionInstruction>> productionSchedule) {
        this.productionSchedule = productionSchedule;
    }

    public Map<Integer, Map<Transit, TransitInstruction>> getTransitSchedule() {
        return transitSchedule;
    }

    public void setTransitSchedule(Map<Integer, Map<Transit, TransitInstruction>> transitSchedule) {
        this.transitSchedule = transitSchedule;
    }

    public Map<Integer, Map<Pair<Item, Plant>, SupplyInstruction>> getSupplySchedule() {
        return supplySchedule;
    }

    public void setSupplySchedule(Map<Integer, Map<Pair<Item, Plant>, SupplyInstruction>> supplySchedule) {
        this.supplySchedule = supplySchedule;
    }

    public Map<Integer, Map<Item, Map<Plant, Inventory>>> getInventoryMap() {
        return inventoryMap;
    }

    public void setInventoryMap(Map<Integer, Map<Item, Map<Plant, Inventory>>> inventoryMap) {
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

    /**
     * Initialise the fields of an empty schedule with the current state/problem.
     * @param state the current state.
     */
    public void initWithState(State state) {
        this.state = state;
        this.startDateId = state.getDateIndex();
        this.endDateId = state.getEnv().getPeriod();

        // initialise all the schedules to be empty
        for (int d = startDateId; d < endDateId; d++) {
            productionSchedule.put(d, new HashMap<>());
            transitSchedule.put(d, new HashMap<>());
            supplySchedule.put(d, new HashMap<>());
        }

        // set the inventory in the start date based on the initial inventories
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            Map<Item, Map<Plant, Inventory>> dailyInventoryMap = new HashMap<>();
            for (Item item : state.getEnv().getItemMap().values()) {
                // for each item
                Map<Plant, Inventory> map = new HashMap<>();

                for (Plant plant : item.getPlants()) {
                    // for each plant that can hold this item
                    map.put(plant, new Inventory(0l, 0l));
                }

                for (Plant plant : item.getInitInventoryMap().keySet()) {
                    long quantity = item.getInitInventoryMap().get(plant);
                    Inventory inventory = map.get(plant);
                    inventory.add(quantity);
                }

                dailyInventoryMap.put(item, map);
            }

            inventoryMap.put(dateId, dailyInventoryMap);
        }

        // add inventory by Raw Material Po
        for (Plant plant : state.getEnv().getPlantMap().values()) {
            Map<Integer, Map<Item, Long>> plantPoMap = plant.getRawMaterialPoMap();

            for (int dateId : plantPoMap.keySet()) {
                Map<Item, Long> dailyPoMap = plantPoMap.get(dateId);

                for (Item item : dailyPoMap.keySet()) {
                    long quantity = dailyPoMap.get(item);

                    addInventory(dateId, item, plant, quantity);
                }
            }
        }

        /**
         * TODO add work-in-process, not frozen production
         */
//        // add inventory by the frozen productions
//        for (Item item : state.getEnv().getItemMap().values()) {
//            Map<Plant, Map<Integer, Long>> itemFPMap = item.getFrozenProductionMap();
//
//            for (Plant plant : itemFPMap.keySet()) {
//                Map<Integer, Long> itemPlantFPMap = itemFPMap.get(plant);
//
//                for (int dateId : itemPlantFPMap.keySet()) {
//                    long quantity = itemPlantFPMap.get(dateId);
//
//                    addInventory(dateId, item, plant, quantity);
//                }
//            }
//        }

        // initialise the order supplies
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            Map<Item, Long> dailyOrderSupply = new HashMap<>();
            orderSupplyMap.put(dateId, dailyOrderSupply);
        }

        // for the ordered demands, the supply may be any date afterwards
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            List<Item> dailyOrderDem = state.getOrderDemMap().get(dateId);

            if (dailyOrderDem != null) {
                for (Item item : dailyOrderDem) {
                    for (int d = dateId; d < endDateId; d++) {
                        orderSupplyMap.get(d).put(item, 0l);
                    }
                }
            }
        }

        // for the forecast demands, the supply is only need for that day
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            Map<Item, Long> dailyForecastSupply = new HashMap<>();

            List<Item> dailyForecastDem = state.getForecastDemMap().get(dateId);

            if (dailyForecastDem != null) {
                for (Item item : dailyForecastDem) {
                    dailyForecastSupply.put(item, 0l);
                }
            }

            forecastSupplyMap.put(dateId, dailyForecastSupply);
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
                    totalInventory += inventoryMap.get(dateId).get(item).get(plant).getTotal();
                }

                inventoryCost += totalInventory * item.getHoldingCost();
            }
        }

        // calculate the total delay based on accumulated delay map
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            for (Item item : accOrderDemMap.get(dateId).keySet()) {
//                System.out.print(accOrderDemMap.get(dateId).get(item) + " ");
//                long old = totalDelay;
                totalDelay += accOrderDemMap.get(dateId).get(item);
//                System.out.println(old + " + " + accOrderDemMap.get(dateId).get(item) + " = " + totalDelay);
            }
//            System.out.println();
        }

        for (int dateId : state.getForecastDemMap().keySet()) {
            for (Item item : state.getForecastDemMap().get(dateId)) {
//                System.out.print(item.getForecastDemandMap().get(dateId) + " ");
//                long old = totalDelay;
                totalDelay += item.getForecastDemandMap().get(dateId);
//                System.out.println(old + " + " + item.getForecastDemandMap().get(dateId) + " = " + totalDelay);
            }
//            System.out.println();
        }
    }

    /**
     * Add the inventory of an item at a plant at a specific date id.
     * The inventory is increased by this amount from that day on.
     * The inventory cost is increase accordingly.
     * @param dateId the date id.
     * @param item the item.
     * @param plant the plant.
     * @param quantity the quantity to be added.
     */
    public void addInventory(int dateId, Item item, Plant plant, long quantity) {
        for (int d = dateId; d < endDateId; d++) {
            Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(d).get(item);

            Inventory old = itemInventoryMap.get(plant);
            old.add(quantity);
        }

        // increase the inventory cost
        inventoryCost += item.getHoldingCost() * quantity * (endDateId-dateId);
    }

    /**
     * Remove the inventory of an item at a plant at a specific date id.
     * The inventory is decreased by this amount from that day on.
     * The inventory cost is decreased accordingly.
     * @param dateId the date id.
     * @param item the item.
     * @param plant the plant.
     * @param quantity the quantity to be removed.
     */
    public void removeInventory(int dateId, Item item, Plant plant, long quantity) {
        for (int d = dateId; d < endDateId; d++) {
            Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(d).get(item);

            Inventory old = itemInventoryMap.get(plant);
            old.remove(quantity);
        }

        // decrease the inventory cost
        inventoryCost -= item.getHoldingCost() * quantity * (endDateId-dateId);
    }

    /**
     * Add a supply to an order demand at a date id.
     * The information will be changed accordingly.
     * @param dateId the date id of the supply.
     * @param supply the supply.
     * @param quantity the quantity to be added.
     */
    public void addOrderSupply(int dateId, Pair<Item, Plant> supply, long quantity) {
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        // add the instruction
        Map<Pair<Item, Plant>, SupplyInstruction> dailySupplySchedule = supplySchedule.get(dateId);
        if (dailySupplySchedule.containsKey(supply)) {
            SupplyInstruction instruction = dailySupplySchedule.get(supply);
            long old = instruction.getQuantity();
            instruction.setQuantity(old+quantity);
        } else {
            SupplyInstruction instruction = new SupplyInstruction(dateId, dateId, item, supply, quantity);
            dailySupplySchedule.put(supply, instruction);
        }

        // remove inventory
        removeInventory(dateId, item, plant, quantity);
        // reduce the free inventory for the previous dates
        for (int d = startDateId; d < dateId; d++) {
            Inventory inventory = inventoryMap.get(d).get(item).get(plant);
            inventory.removeFree(quantity);
        }

        // update the order supply map
        Map<Item, Long> itemSupplyMap = orderSupplyMap.get(dateId);
        long old = itemSupplyMap.get(item);
        itemSupplyMap.put(item, old+quantity);

        // decrease the accumulated order demand and total delay from this day on
        for (int d = dateId; d < endDateId; d++) {
            Map<Item, Long> dailyAccOrderDem = accOrderDemMap.get(d);
            old = dailyAccOrderDem.get(item);
            dailyAccOrderDem.put(item, old-quantity);
        }

        totalDelay -= quantity * (endDateId-dateId);
    }

    /**
     * Remove a supply to an order demand at a date id.
     * @param dateId the date id.
     * @param supply the supply.
     * @param quantity the quantity to be removed.
     */
    public void removeOrderSupply(int dateId, Pair<Item, Plant> supply, long quantity) {
        SupplyInstruction instruction = supplySchedule.get(dateId).get(supply);
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        long old = instruction.getQuantity();
        if (old == quantity) {
            supplySchedule.remove(instruction);
        }
        else {
            instruction.setQuantity(old-quantity);
        }

        // add inventory
        addInventory(dateId, item, plant, quantity);
        // increase the free inventory for the previous dates
        for (int d = startDateId; d < dateId; d++) {
            Inventory inventory = inventoryMap.get(d).get(item).get(plant);
            inventory.addFree(quantity);
        }

        // update order supply map
        Map<Item, Long> itemSupplyMap = orderSupplyMap.get(dateId);
        old = itemSupplyMap.get(item);
        itemSupplyMap.put(item, old-quantity);

        // increase the accumulated order demand and total delay from this day on
        for (int d = dateId; d < endDateId; d++) {
            Map<Item, Long> dailyAccOrderDem = accOrderDemMap.get(d);

            old = dailyAccOrderDem.get(item);
            dailyAccOrderDem.put(item, old+quantity);
        }

        totalDelay += quantity * (endDateId-dateId);
    }

    /**
     * Add a supply to a forecast demand at a date id.
     * @param dateId the date id.
     * @param supply the supply.
     * @param quantity the quantity.
     */
    public void addForecastSupply(int dateId, Pair<Item, Plant> supply, long quantity) {
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        // add the instruction
        Map<Pair<Item, Plant>, SupplyInstruction> dailySupplySchedule = supplySchedule.get(dateId);
        if (dailySupplySchedule.containsKey(supply)) {
            SupplyInstruction instruction = dailySupplySchedule.get(supply);
            long old = instruction.getQuantity();
            instruction.setQuantity(old+quantity);
        } else {
            SupplyInstruction instruction = new SupplyInstruction(dateId, dateId, item, supply, quantity);
            dailySupplySchedule.put(supply, instruction);
        }

        // remove inventory
        removeInventory(dateId, item, plant, quantity);
        // reduce the free inventory for the previous dates
        for (int d = startDateId; d < dateId; d++) {
            Inventory inventory = inventoryMap.get(d).get(item).get(plant);
            inventory.removeFree(quantity);
        }

        // update the forecast supply map
        Map<Item, Long> itemSupplyMap = forecastSupplyMap.get(dateId);
        long old = itemSupplyMap.get(item);
        itemSupplyMap.put(item, old+quantity);

        // decrease the total delay by the quantity
        totalDelay -= quantity;
    }

    /**
     * Remove a supply to a forecast demand at a date id.
     * @param dateId the date id.
     * @param supply the supply.
     * @param quantity the quantity.
     */
    public void removeForecastSupply(int dateId, Pair<Item, Plant> supply, long quantity) {
        SupplyInstruction instruction = supplySchedule.get(dateId).get(supply);
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        long old = instruction.getQuantity();
        if (old == quantity) {
            supplySchedule.remove(instruction);
        }
        else {
            instruction.setQuantity(old-quantity);
        }

        // add inventory
        addInventory(dateId, item, plant, quantity);
        // increase the free inventory for the previous dates
        for (int d = startDateId; d < dateId; d++) {
            Inventory inventory = inventoryMap.get(d).get(item).get(plant);
            inventory.addFree(quantity);
        }

        // update the forecast supply map
        Map<Item, Long> itemSupplyMap = forecastSupplyMap.get(dateId);
        old = itemSupplyMap.get(item);
        itemSupplyMap.put(item, old-quantity);

        // increase the total delay
        totalDelay += quantity;
    }

    /**
     * Supply the order demand of an item at a dateId.
     * The supply is directly using the current inventory.
     * @param item the item.
     * @param dateId the date id.
     */
    public void supplyOrderDemand(Item item, int dateId) {
        Map<Item, Long> dailyAccOrderDem = accOrderDemMap.get(dateId);

        long itemOrderDemand = dailyAccOrderDem.get(item);
        long remaining = itemOrderDemand;

        // check the current inventory of the item in different plants
        Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(dateId).get(item);
        for (Plant plant : itemInventoryMap.keySet()) {
            long inventory = itemInventoryMap.get(plant).getFree();

            if (inventory == 0)
                continue;

            // supply from this plant until the order is cleaned
            long supplied = inventory;
            if (supplied > remaining)
                supplied = remaining;

            Pair<Item, Plant> supply = new Pair<>(item, plant);
            addOrderSupply(dateId, supply, supplied);

            remaining -= supplied;

            if (remaining == 0)
                break;
        }
    }

    /**
     * Supply a forecast demand at a date id.
     * The supply is directly using the current inventory.
     * @param item the item demanded.
     * @param dateId the date id.
     */
    public void supplyForecastDemand(Item item, int dateId) {
        long itemForecastDemand = item.getForecastDemandMap().get(dateId);
        long remaining = itemForecastDemand;

        // check the current inventory of the item in different plants
        Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(dateId).get(item);
        for (Plant plant : itemInventoryMap.keySet()) {
            long inventory = itemInventoryMap.get(plant).getFree();

            if (inventory == 0)
                continue;

            // supply from this plant until the order is cleaned
            long supplied = inventory;
            if (supplied > remaining)
                supplied = remaining;

            Pair<Item, Plant> supply = new Pair<>(item, plant);
            addForecastSupply(dateId, supply, supplied);

            remaining -= supplied;

            if (remaining == 0)
                break;
        }
    }

    /**
     * Calculate the maximal production quantity of a production in a date id.
     * The maximal quantity depends on
     *   (1) the remaining capacity of the machine;
     *   (2) the inventory of the boms;
     *   (3) the maximal production.
     * @param production the production.
     * @param dateId the date id.
     * @return the maximal production quantity.
     */
    public long maxProductionQuantity(Production production, int dateId) {
        Item item = production.getItem();
        Plant plant = production.getPlant();
        List<Bom> assembly = production.getAssembly();
        // get the remaining capacity of the machine for this production
        double remaCapa = item.getMachineMap().get(plant).getCapacityMap().get(dateId).getRemaining();
        long maxQuantity1 = (long)(remaCapa / item.getRate());

        // check the inventory of the boms at the plant
        List<Long> bomInventories = new ArrayList<>();
        for (Bom bom : assembly) {
            long bomInve = inventoryMap.get(dateId).get(bom.getComponent()).get(plant).getFree();
            bomInventories.add(bomInve);
        }

        long maxQuantity2 = Long.MAX_VALUE;
        for (int i = 0; i < assembly.size(); i++) {
            long quantity = bomInventories.get(i) / assembly.get(i).getQuantity();

            if (maxQuantity2 > quantity)
                maxQuantity2 = quantity;
        }

        long maxQuantity = maxQuantity1;
        if (maxQuantity > maxQuantity2)
            maxQuantity = maxQuantity2;

        if (maxQuantity > production.getMaxProduction())
            maxQuantity = production.getMaxProduction();

        return maxQuantity;
    }

    public void addProduction(int dateId, Production production, long lots) {
        int prodStartDate = dateId;
        int prodEndDate = prodStartDate+production.getLeadTime();
        Item item = production.getItem();
        Plant plant = production.getPlant();
        long prodQuantity = production.getLotSize() * lots;

        // add the instruction
        Map<Production, ProductionInstruction> dailyProdSchedule = productionSchedule.get(dateId);
        if (dailyProdSchedule.containsKey(production)) {
            ProductionInstruction instruction = dailyProdSchedule.get(production);
            long old = instruction.getLots();
            instruction.setLots(old+lots);
        } else {
            ProductionInstruction instruction =
                    new ProductionInstruction(prodStartDate, prodEndDate, item, production, lots);
            dailyProdSchedule.put(production, instruction);
        }

        // remove the capacity of the machine in the production start date
        MachineSet machineSet = item.getMachineMap().get(plant);
        Capacity capacity = machineSet.getCapacityMap().get(prodStartDate);
        double old = capacity.getRemaining();
        capacity.setRemaining(old-item.getRate()*prodQuantity);

        // remove the inventory of the boms when the production is finished
        for (Bom bom : production.getAssembly()) {
            Item component = bom.getComponent();
            long quantity = prodQuantity * bom.getQuantity();
            removeInventory(prodEndDate, component, plant, quantity);
        }

        // add the inventory of the produced item when the production is finished
        addInventory(prodEndDate, item, plant, prodQuantity);

        // add the production cost
        productionCost += production.getCost() * lots;
    }
}
