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
    private Map<SupplyChain, Double> supplyChainMap;

    private Map<Integer, Map<Item, Map<Plant, Inventory>>> inventoryMap; // the inventory of each item at each plant in each day
    private Map<Integer, Map<Item, Double>> supplyMap;
    private Map<Integer, Map<Item, Double>> accOrderDemMap; // accumulated order demand each day

    private double holdingCost;
    private double productionCost;
    private double transitCost;
    private double totalDelay;

    private double fillRate; // the fill rate over the scheduling period.

    /**
     * Construct an empty schedule without any other information.
     */
    public Schedule() {
        productionSchedule = new HashMap<>();
        transitSchedule = new HashMap<>();
        supplySchedule = new HashMap<>();
        supplyChainMap = new HashMap<>();

        inventoryMap = new HashMap<>();
        supplyMap = new HashMap<>();
        accOrderDemMap = new HashMap<>();

        holdingCost = 0d;
        productionCost = 0d;
        transitCost = 0d;
        totalDelay = 0d;
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

    public Map<SupplyChain, Double> getSupplyChainMap() {
        return supplyChainMap;
    }

    public void setSupplyChainMap(Map<SupplyChain, Double> supplyChainMap) {
        this.supplyChainMap = supplyChainMap;
    }

    public Map<Integer, Map<Item, Map<Plant, Inventory>>> getInventoryMap() {
        return inventoryMap;
    }

    public void setInventoryMap(Map<Integer, Map<Item, Map<Plant, Inventory>>> inventoryMap) {
        this.inventoryMap = inventoryMap;
    }

    public Map<Integer, Map<Item, Double>> getSupplyMap() {
        return supplyMap;
    }

    public void setSupplyMap(Map<Integer, Map<Item, Double>> supplyMap) {
        this.supplyMap = supplyMap;
    }

    public Map<Integer, Map<Item, Double>> getAccOrderDemMap() {
        return accOrderDemMap;
    }

    public void setAccOrderDemMap(Map<Integer, Map<Item, Double>> accOrderDemMap) {
        this.accOrderDemMap = accOrderDemMap;
    }

    public double getHoldingCost() {
        return holdingCost;
    }

    public void setHoldingCost(double holdingCost) {
        this.holdingCost = holdingCost;
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

    public double getTotalDelay() {
        return totalDelay;
    }

    public void setTotalDelay(double totalDelay) {
        this.totalDelay = totalDelay;
    }

    public double getFillRate() {
        return fillRate;
    }

    public void setFillRate(double fillRate) {
        this.fillRate = fillRate;
    }

    /**
     * Calculate the fill rate of the schedule.
     */
    public void calcFillRate() {
        fillRate = 0;
        double denominator = 0d;

        // the delayed demand map, initially empty
        Map<Item, Double> delayedDemMap = new HashMap<>();

        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            // only consider the item with (order + forecast > 0)
            // get their order demand and forecast demand
            Map<Item, Double> orderDemMap = new HashMap<>();
            Map<Item, Double> forecastDemMap = new HashMap<>();
            if (state.getOrderDemMap().containsKey(dateId)) {
                for (Item item : state.getOrderDemMap().get(dateId)) {
                    orderDemMap.put(item, item.getOrderDemandMap().get(dateId));

                    if (state.getForecastDemMap().containsKey(dateId)) {
                        if (state.getForecastDemMap().get(dateId).contains(item))
                            continue;

                        forecastDemMap.put(item, 0d);
                    }
                }
            }

            if (state.getForecastDemMap().containsKey(dateId)) {
                for (Item item : state.getForecastDemMap().get(dateId)) {
                    forecastDemMap.put(item, item.getForecastDemandMap().get(dateId));

                    if (state.getOrderDemMap().containsKey(dateId)) {
                        if (state.getOrderDemMap().get(dateId).contains(item))
                            continue;

                        orderDemMap.put(item, 0d);
                    }
                }
            }

            Map<Item, Double> dailySupplyMap = supplyMap.get(dateId);

            for (Item item : orderDemMap.keySet()) {
                double totalDemand = orderDemMap.get(item)+forecastDemMap.get(item);

                // get the delayed demand before supply
                double delayed = 0;
                if (delayedDemMap.containsKey(item))
                    delayed = delayedDemMap.get(item);

                // get the supply
                double supply = 0;
                if (dailySupplyMap.containsKey(item))
                    supply = dailySupplyMap.get(item);

                // calculate the fill rate
                double fr = 0d;
                if (supply > delayed)
                    fr = 1.0*(supply-delayed)/totalDemand;

                fillRate += fr;
                denominator ++;

                // update the delayed demand
                double newDelay = orderDemMap.get(item)+delayed-supply;
                if (newDelay < 0)
                    newDelay = 0;
                delayedDemMap.put(item, newDelay);
            }
        }

        fillRate /= denominator;
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
                    double quantity = item.getInitInventoryMap().get(plant);
                    Inventory inventory = map.get(plant);
                    inventory.add(quantity);
                }

                dailyInventoryMap.put(item, map);
            }

            inventoryMap.put(dateId, dailyInventoryMap);
        }

        // add inventory by Raw Material Po
        for (Plant plant : state.getEnv().getPlantMap().values()) {
            Map<Integer, Map<Item, Double>> plantPoMap = plant.getRawMaterialPoMap();

            for (int dateId : plantPoMap.keySet()) {
                Map<Item, Double> dailyPoMap = plantPoMap.get(dateId);

                for (Item item : dailyPoMap.keySet()) {
                    double quantity = dailyPoMap.get(item);

                    addInventory(dateId, item, plant, quantity);
                }
            }
        }

        // add inventory by the work-in-process
        for (Plant plant : state.getEnv().getPlantMap().values()) {
            Map<Integer, Map<Item, Double>> plantWipMap = plant.getWorkInProcessMap();

            for (int dateId : plantWipMap.keySet()) {
                Map<Item, Double> dailyWipMap = plantWipMap.get(dateId);

                for (Item item : dailyWipMap.keySet()) {
                    double quantity = dailyWipMap.get(item);

                    addInventory(dateId, item, plant, quantity);
                }
            }
        }

        // initialise the supplies
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            Map<Item, Double> dailySupply = new HashMap<>();
            supplyMap.put(dateId, dailySupply);
        }

        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            List<Item> dailyOrderDem = state.getOrderDemMap().get(dateId);
            List<Item> dailyForecastDem = state.getForecastDemMap().get(dateId);

            if (dailyOrderDem != null) {
                for (Item item : dailyOrderDem) {
                    // for the ordered demands, the supply may be any date afterwards
                    for (int d = dateId; d < endDateId; d++) {
                        supplyMap.get(d).put(item, 0d);
                    }
                }
            }

            if (dailyForecastDem != null) {
                for (Item item : dailyForecastDem) {
                    // for the forecast demands, the supply is only need for that day
                    supplyMap.get(dateId).put(item, 0d);
                }
            }
        }

        // initialise the accumulated order demand map
        // the accumulated order demand adds up all the leftover order demands
        Map<Item, Double> leftover = new HashMap<>();
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            Map<Item, Double> dailyAccOrderDem = new HashMap<>(leftover);

            List<Item> dailyOrderDem = state.getOrderDemMap().get(dateId);

            if (dailyOrderDem != null) {
                for (Item item : dailyOrderDem) {
                    if (dailyAccOrderDem.containsKey(item)) {
                        double old = dailyAccOrderDem.get(item);
                        dailyAccOrderDem.put(item, old+item.getOrderDemandMap().get(dateId));
                    } else {
                        dailyAccOrderDem.put(item, item.getOrderDemandMap().get(dateId));
                    }
                }
            }

            leftover = dailyAccOrderDem;

            accOrderDemMap.put(dateId, dailyAccOrderDem);
        }

//        // calculate the remaining demand maps
//        remOrderDemMap = new HashMap<>();
//        remForcastDemMap = new HashMap<>();
//
//        for (Item item : state.getEnv().getItemMap().values()) {
//            TreeMap<Integer, Demand> odMap = new TreeMap<>();
//
//            for (int dateId : item.getOrderDemandMap().keySet()) {
//                Demand od = new OrderDemand(dateId, item, item.getOrderDemandMap().get(dateId));
//                odMap.put(dateId, od);
//            }
//
//            TreeMap<Integer, Demand> fdMap = new TreeMap<>();
//            for (int dateId : item.getForecastDemandMap().keySet()) {
//                Demand fd = new ForecastDemand(dateId, item, item.getForecastDemandMap().get(dateId));
//                fdMap.put(dateId, fd);
//            }
//
//            if (!odMap.isEmpty())
//                remOrderDemMap.put(item, odMap);
//
//            if (!fdMap.isEmpty())
//                remForcastDemMap.put(item, fdMap);
//        }

        // calculate the holding cost by summing up for all the days
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            for (Item item : inventoryMap.get(dateId).keySet()) {
                double totalInventory = 0;

                for (Plant plant : inventoryMap.get(dateId).get(item).keySet()) {
                    totalInventory += inventoryMap.get(dateId).get(item).get(plant).getTotal();
                }

                holdingCost += totalInventory * item.getHoldingCost();
            }
        }

        // calculate the total delay based on accumulated delay map
        for (int dateId = startDateId; dateId < endDateId; dateId++) {
            for (Item item : accOrderDemMap.get(dateId).keySet()) {
//                System.out.print(accOrderDemMap.get(dateId).get(item) + " ");
//                double old = totalDelay;
                totalDelay += accOrderDemMap.get(dateId).get(item);
//                System.out.println(old + " + " + accOrderDemMap.get(dateId).get(item) + " = " + totalDelay);
            }
//            System.out.println();
        }

        for (int dateId : state.getForecastDemMap().keySet()) {
            for (Item item : state.getForecastDemMap().get(dateId)) {
//                System.out.print(item.getForecastDemandMap().get(dateId) + " ");
//                double old = totalDelay;
                totalDelay += item.getForecastDemandMap().get(dateId);
//                System.out.println(old + " + " + item.getForecastDemandMap().get(dateId) + " = " + totalDelay);
            }
//            System.out.println();
        }
    }

    /**
     * Add the inventory of an item at a plant at a specific date id.
     * The inventory is increased by this amount from that day on.
     * The holding cost is increase accordingly.
     * @param dateId the date id.
     * @param item the item.
     * @param plant the plant.
     * @param quantity the quantity to be added.
     */
    public void addInventory(int dateId, Item item, Plant plant, double quantity) {
        for (int d = dateId; d < endDateId; d++) {
            Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(d).get(item);

            Inventory old = itemInventoryMap.get(plant);
            old.add(quantity);
        }

        // set the free inventories of all previous days to the new free inventory
        // if a previous day's inventory is smaller than the free, the stop there.
        double free = inventoryMap.get(dateId).get(item).get(plant).getFree();
        for (int d = dateId-1; d >= startDateId; d--) {
            Inventory inventory = inventoryMap.get(d).get(item).get(plant);

            if (inventory.getTotal() < free)
                break;

            inventory.setFree(free);
        }

        // increase the holding cost
        holdingCost += item.getHoldingCost() * quantity * (endDateId-dateId);
    }

    /**
     * Remove the inventory of an item at a plant at a specific date id.
     * The inventory is decreased by this amount from that day on.
     * The holding cost is decreased accordingly.
     * @param dateId the date id.
     * @param item the item.
     * @param plant the plant.
     * @param quantity the quantity to be removed.
     */
    public void removeInventory(int dateId, Item item, Plant plant, double quantity) {
        for (int d = dateId; d < endDateId; d++) {
            Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(d).get(item);

            Inventory old = itemInventoryMap.get(plant);
            old.remove(quantity);
        }

        // set the free inventories of all previous days to the new free inventory
        // if the original free inventory is larger, then it will set to the new free.
        double free = inventoryMap.get(dateId).get(item).get(plant).getFree();
        for (int d = dateId-1; d >= startDateId; d--) {
            Inventory inventory = inventoryMap.get(d).get(item).get(plant);

            if (inventory.getFree() < free)
                break;

            inventory.setFree(free);
        }

        // decrease the holding cost
        holdingCost -= item.getHoldingCost()*quantity*(endDateId-dateId);
    }

    /**
     * Add a supply to an order demand at a date id.
     * The information will be changed accordingly.
     * @param dateId the date id of the supply.
     * @param supply the supply.
     * @param quantity the quantity to be added.
     */
    public void addOrderSupply(int dateId, Pair<Item, Plant> supply, double quantity) {
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        // add the instruction
        Map<Pair<Item, Plant>, SupplyInstruction> dailySupplySchedule = supplySchedule.get(dateId);
        if (dailySupplySchedule.containsKey(supply)) {
            SupplyInstruction instruction = dailySupplySchedule.get(supply);
            double old = instruction.getQuantity();
            instruction.setQuantity(old+quantity);
        } else {
            SupplyInstruction instruction = new SupplyInstruction(dateId, dateId, item, supply, quantity);
            dailySupplySchedule.put(supply, instruction);
        }

        // remove inventory
        removeInventory(dateId, item, plant, quantity);

        // update the supply map
        Map<Item, Double> itemSupplyMap = supplyMap.get(dateId);
        double old = itemSupplyMap.get(item);
        itemSupplyMap.put(item, old+quantity);

        // decrease the accumulated order demand and total delay from this day on
        // if the future demand is supplied, then revert the supply
        for (int d = dateId; d < endDateId; d++) {
            Map<Item, Double> dailyAccOrderDem = accOrderDemMap.get(d);
            old = dailyAccOrderDem.get(item);
            dailyAccOrderDem.put(item, old-quantity);

            if (old < quantity) {
                double reverted = quantity-old;
                // find the corresponding supply instruction
                Map<Pair<Item, Plant>, SupplyInstruction> futureSupplySchedule = supplySchedule.get(d);
                for (Pair<Item, Plant> pair : futureSupplySchedule.keySet()) {
                    if (pair.getFirst().equals(item)) {
                        // reduce the supply from this plant
                        SupplyInstruction revertedInstruction = futureSupplySchedule.get(pair);
                        double revertedFromPlant = revertedInstruction.getQuantity();

                        if (revertedFromPlant > reverted)
                            revertedFromPlant = reverted;

                        removeOrderSupply(d, pair, revertedFromPlant);

                        reverted -= revertedFromPlant;

                        if (reverted == 0)
                            break;
                    }
                }
            }
        }

        totalDelay -= quantity * (endDateId-dateId);
    }

    /**
     * Remove a supply to an order demand at a date id.
     * @param dateId the date id.
     * @param supply the supply.
     * @param quantity the quantity to be removed.
     */
    public void removeOrderSupply(int dateId, Pair<Item, Plant> supply, double quantity) {
        SupplyInstruction instruction = supplySchedule.get(dateId).get(supply);
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        double old = instruction.getQuantity();
        if (old == quantity) {
            supplySchedule.remove(instruction);
        }
        else {
            instruction.setQuantity(old-quantity);
        }

        // add inventory
        addInventory(dateId, item, plant, quantity);

        // update supply map
        Map<Item, Double> itemSupplyMap = supplyMap.get(dateId);
        old = itemSupplyMap.get(item);
        itemSupplyMap.put(item, old-quantity);

        // increase the accumulated order demand and total delay from this day on
        for (int d = dateId; d < endDateId; d++) {
            Map<Item, Double> dailyAccOrderDem = accOrderDemMap.get(d);

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
    public void addForecastSupply(int dateId, Pair<Item, Plant> supply, double quantity) {
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        // add the instruction
        Map<Pair<Item, Plant>, SupplyInstruction> dailySupplySchedule = supplySchedule.get(dateId);
        if (dailySupplySchedule.containsKey(supply)) {
            SupplyInstruction instruction = dailySupplySchedule.get(supply);
            double old = instruction.getQuantity();
            instruction.setQuantity(old+quantity);
        } else {
            SupplyInstruction instruction = new SupplyInstruction(dateId, dateId, item, supply, quantity);
            dailySupplySchedule.put(supply, instruction);
        }

        // remove inventory
        removeInventory(dateId, item, plant, quantity);

        // update the supply map
        Map<Item, Double> itemSupplyMap = supplyMap.get(dateId);
        double old = itemSupplyMap.get(item);
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
    public void removeForecastSupply(int dateId, Pair<Item, Plant> supply, double quantity) {
        SupplyInstruction instruction = supplySchedule.get(dateId).get(supply);
        Item item = supply.getFirst();
        Plant plant = supply.getSecond();

        double old = instruction.getQuantity();
        if (old == quantity) {
            supplySchedule.remove(instruction);
        }
        else {
            instruction.setQuantity(old-quantity);
        }

        // add inventory
        addInventory(dateId, item, plant, quantity);

        // update the supply map
        Map<Item, Double> itemSupplyMap = supplyMap.get(dateId);
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
        Map<Item, Double> dailyAccOrderDem = accOrderDemMap.get(dateId);

        double itemOrderDemand = dailyAccOrderDem.get(item);
        double remaining = itemOrderDemand;

        // check the current inventory of the item in different plants
        Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(dateId).get(item);
        for (Plant plant : itemInventoryMap.keySet()) {
            double inventory = itemInventoryMap.get(plant).getFree();

            if (inventory == 0)
                continue;

            // supply from this plant until the order is cleaned
            double supplied = inventory;
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
        double itemForecastDemand = item.getForecastDemandMap().get(dateId);
        double remaining = itemForecastDemand;

        // check the current inventory of the item in different plants
        Map<Plant, Inventory> itemInventoryMap = inventoryMap.get(dateId).get(item);
        for (Plant plant : itemInventoryMap.keySet()) {
            double inventory = itemInventoryMap.get(plant).getFree();

            if (inventory == 0)
                continue;

            // supply from this plant until the order is cleaned
            double supplied = inventory;
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
     * Add a production to the schedule in a specific start date id.
     * @param dateId the start date id.
     * @param production the added production.
     * @param lots the lots of the production.
     */
    public void addProduction(int dateId, Production production, long lots) {
        int prodStartDate = dateId;
        int prodEndDate = prodStartDate+production.getLeadTime();
        Item item = production.getItem();
        Plant plant = production.getPlant();
        double quantity = production.getLotSize()*lots;

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

        // remove the capacity of the machines in the production start date
        item.reduceCapacity(plant, prodStartDate, quantity);

        // remove the inventory of the boms when the production is finished
        for (BomComponent bomComponent : production.getBom()) {
            Item component = bomComponent.getMaterial();
            double bomQuantity = quantity*bomComponent.getQuantity();
            removeInventory(prodEndDate, component, plant, bomQuantity);

            // add the holding cost of the bom during the production
            holdingCost += component.getHoldingCost()*bomQuantity*(prodEndDate-prodStartDate);
        }

        // add the inventory of the produced item when the production is finished
        addInventory(prodEndDate, item, plant, quantity);

        // add the production cost
        productionCost += production.getCost()*lots*production.getLotSize();
    }

    /**
     * Add a transit to the schedule starting in a specific date.
     * @param dateId the start date id.
     * @param transit the transit.
     * @param quantity the quantity.
     */
    public void addTransit(int dateId, Transit transit, double quantity) {
        int tranStartDate = dateId;
        int tranEndDate = tranStartDate+transit.getLeadTime();
        Item item = transit.getItem();
        Plant fromPlant = transit.getFromPlant();
        Plant toPlant = transit.getToPlant();

        // add the instruction
        Map<Transit, TransitInstruction> dailyTranSchedule = transitSchedule.get(dateId);
        if (dailyTranSchedule.containsKey(transit)) {
            TransitInstruction instruction = dailyTranSchedule.get(transit);
            double old = instruction.getQuantity();
            instruction.setQuantity(old+quantity);
        } else {
            TransitInstruction instruction =
                    new TransitInstruction(tranStartDate, tranEndDate, item, transit, quantity);
            dailyTranSchedule.put(transit, instruction);
        }

        // remove the inventory of the item in the source plant
        removeInventory(tranStartDate, item, fromPlant, quantity);

        // add the inventory of the item in the target plant
        addInventory(tranEndDate, item, toPlant, quantity);

        // add the holding cost during the transit
        holdingCost += item.getHoldingCost()*quantity*(tranEndDate-tranStartDate);

        // add the transit cost
        transitCost += transit.getCost()*quantity;
    }
}
