package scheduling.core;

import org.apache.commons.math3.util.Pair;
import scheduling.core.input.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A supply chain is a chain of supplies/productions/transits.
 * There are three types of streams to provide the items:
 *   (1) inventory stream: directly provide from the plant's inventory.
 *   (2) transit stream: provide by transitting from other plants.
 *   (3) production stream: provide by production at the plant.
 *
 * The streams may not all be active.
 * A stream is providing the items only if it is active.
 */
public class SupplyChain implements Comparable<SupplyChain> {
    protected int dateId; // the date id that the supply chain can provide the item
    protected Item item; // the final item provided by this supply chain.
    protected Plant plant; // the plant that provides the item.
    protected boolean active; // whether it is active or not.
    protected boolean prodActive; // whether the production stream is active or not.
    protected double maxQuantity; // the max quantity of all the current active streams.
    protected long maxProdQuantity; // the max production quantity of the active stream

    protected double inventory; // the free inventory of the item in the plant.
    protected Production production; // the production
    protected Map<BomComponent, SupplyChain> bomStreamMap;
    protected Map<Transit, SupplyChain> transitStreamMap;

    protected int leadTime; // the lead time of the supply chain with the active streams
    protected double holdingCost; // the holding cost changed by the supply chain
    protected double productionCost; // the production cost changed by the supply chain
    protected double transitCost; // the transit cost changed by the supply chain
    protected double totalCost; // the total cost changed by the supply chain

    protected int length; // the length of the chain (number of steps)

    protected double priority;

    public SupplyChain(Item item, Plant plant) {
        this.item = item;
        this.plant = plant;
        active = false; // default, inactive
        prodActive = false;

        // initialise the production stream, if there exists
        bomStreamMap = new HashMap<>();
        production = item.getProductionMap().get(plant);
        // the bom stream will be linked globally later on

        // the transit stream map will be linked globally later on
        transitStreamMap = new HashMap<>();
    }

    public int getDateId() {
        return dateId;
    }

    public void setDateId(int dateId) {
        this.dateId = dateId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isProdActive() {
        return prodActive;
    }

    public void setProdActive(boolean prodActive) {
        this.prodActive = prodActive;
    }

    public double getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(double maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public double getInventory() {
        return inventory;
    }

    public void setInventory(double inventory) {
        this.inventory = inventory;
    }

    public long getMaxProdQuantity() {
        return maxProdQuantity;
    }

    public void setMaxProdQuantity(long maxProdQuantity) {
        this.maxProdQuantity = maxProdQuantity;
    }

    public Map<Transit, SupplyChain> getTransitStreamMap() {
        return transitStreamMap;
    }

    public void setTransitStreamMap(Map<Transit, SupplyChain> transitStreamMap) {
        this.transitStreamMap = transitStreamMap;
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public Map<BomComponent, SupplyChain> getBomStreamMap() {
        return bomStreamMap;
    }

    public void setBomStreamMap(Map<BomComponent, SupplyChain> bomStreamMap) {
        this.bomStreamMap = bomStreamMap;
    }

    public int getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(int leadTime) {
        this.leadTime = leadTime;
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

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * To activate the streams of this supply chain,
     * given a schedule and the date to provide the item.
     * This will update the max quantity the supply chain can provide,
     * and the lead time of this supply chain.
     * Then, it will activate the streams with the smallest lead time.
     * It will also calculate the length of the supply chain
     *
     * @param schedule the schedule.
     * @param dateId the date id.
     * @param visited the visited supply chains to be skipped.
     */
    public void activateStreams(Schedule schedule, int dateId, Set<SupplyChain> visited) {
        if (visited.contains(this))
            return;

        visited.add(this);

        // reset the activation status
        active = false;
        prodActive = false;

        // check whether there is direct supply from the plant's inventory
        inventory = schedule.getInventoryMap().get(dateId).get(item).get(plant).getFree();

        if (inventory > 0) {
            // if there is direct supply, then do not consider any other
            // the lead time becomes 0
            active = true;
            this.dateId = dateId;
            leadTime = 0;
            maxQuantity = inventory;
        } else {
            leadTime = Integer.MAX_VALUE;
            // there is no supply, check transit and production streams
            for (Transit transit : transitStreamMap.keySet()) {
                int transitStartTime = dateId-transit.getLeadTime();

                // the transit cannot start in time, skip.
                if (transitStartTime < schedule.getStartDateId())
                    continue;

                SupplyChain chain = transitStreamMap.get(transit);
                chain.activateStreams(schedule, transitStartTime, visited);

                if (chain.getMaxQuantity() == 0) {
                    // if this transit stream cannot provide any, do nothing
                    // the stream will not be active
                    continue;
                }

                int totalLeadTime = chain.getLeadTime()+transit.getLeadTime();

                if (leadTime > totalLeadTime) {
                    // find a new stream with smaller lead time
                    leadTime = totalLeadTime;
                }
            }

            int prodLeadTime = Integer.MAX_VALUE;

            if (production != null) {
                int prodStartTime = dateId-production.getLeadTime();

                // the production has to be started in time.
                if (prodStartTime >= schedule.getStartDateId()) {
                    maxProdQuantity = production.maxQuantityFromCapacity(prodStartTime);
                    if (maxProdQuantity > production.getMaxProduction())
                        maxProdQuantity = production.getMaxProduction();

                    for (BomComponent component : bomStreamMap.keySet()) {
                        SupplyChain chain = bomStreamMap.get(component);
                        chain.activateStreams(schedule, prodStartTime, visited);

                        long q = (long)(chain.getMaxQuantity()/component.getQuantity());

                        if (maxProdQuantity > q)
                            maxProdQuantity = q;

                        if (maxProdQuantity == 0) {
                            // if a stream cannot provide sufficient bom component, stop
                            // the production stream will not be active
                            break;
                        }
                    }

                    if (maxProdQuantity > 0) {
                        prodLeadTime = production.getLeadTime();
                        for (SupplyChain chain : bomStreamMap.values()) {
                            int totalLeadTime = chain.getLeadTime()+production.getLeadTime();

                            if (prodLeadTime < totalLeadTime)
                                prodLeadTime = totalLeadTime;
                        }

                        if (leadTime > prodLeadTime)
                            leadTime = prodLeadTime;
                    }
                }
            }

            // activate the streams
            maxQuantity = 0d;
            if (leadTime < Integer.MAX_VALUE) {
                active = true;
                this.dateId = dateId;

                // active all the streams with smallest lead time
                for (Transit transit : transitStreamMap.keySet()) {
                    SupplyChain chain = transitStreamMap.get(transit);

                    if (chain.getMaxQuantity() == 0)
                        continue;

                    if (chain.getLeadTime()+transit.getLeadTime() == leadTime) {
                        maxQuantity += chain.getMaxQuantity();
                        chain.setActive(true);
                    }
                }

                if (prodLeadTime == leadTime) {
                    maxQuantity += maxProdQuantity;
                    // activate the production stream and all the bom streams
                    prodActive = true;

                    for (SupplyChain chain : bomStreamMap.values()) {
                        chain.setActive(true);
                    }
                }
            }
        }

        // calculate the costs based on the active streams
        calcHoldingCost(schedule, dateId, new HashSet<>());
        calcProductionCost(new HashSet<>());
        calcTransitCost(new HashSet<>());

        totalCost = holdingCost+productionCost+transitCost;

        // calculate the length based on the active streams
        length = 0;

        if (inventory > 0) // if there is direct supply, then length = 0
            return;

        if (prodActive) {
            // check the active production stream
            int bomLength = 0;

            for (SupplyChain chain : bomStreamMap.values()) {
                if (bomLength < chain.getLength())
                    bomLength = chain.getLength();
            }

            int prodLength = 1+bomLength;

            if (length < prodLength)
                length = prodLength;
        }

        for (SupplyChain chain : transitStreamMap.values()) {
            if (chain.isActive()) {
                // check the active transit streams
                int transitLength = 1+chain.getLength();

                if (length < transitLength)
                    length = transitLength;
            }
        }
    }

    /**
     * Calculate the holding cost for providing one item by the supply chain.
     * @param schedule the schedule.
     * @param dateId the date id to provide the item.
     * @param visited the visited supply chains, that can be skipped.
     */
    public void calcHoldingCost(Schedule schedule, int dateId, Set<SupplyChain> visited) {
        if (visited.contains(this))
            return;

        visited.add(this);

        holdingCost = 0d;

        if (!active)
            return;

        if (inventory > 0) {
            // decrease the holding cost of the supplied item
            holdingCost = -item.getHoldingCost()*(schedule.getEndDateId()-dateId);
        }

        if (prodActive) {
            for (BomComponent component : bomStreamMap.keySet()) {
                SupplyChain chain = bomStreamMap.get(component);
                chain.calcHoldingCost(schedule, dateId, visited);
                // the holding cost changed by supplying the bom component
                holdingCost += chain.getHoldingCost()*component.getQuantity();
            }
        }

        for (SupplyChain chain : transitStreamMap.values()) {
            // to avoid loop
            if (chain.length > length)
                continue;

            if (!chain.isActive())
                continue;

            chain.calcHoldingCost(schedule, dateId, visited);
            holdingCost += chain.getHoldingCost();
        }
    }

    /**
     * Calculate the production cost to providing one item by this supply chain.
     * @param visited the visited supply chains to be skipped.
     */
    public void calcProductionCost(Set<SupplyChain> visited) {
        if (visited.contains(this))
            return;

        visited.add(this);

        productionCost = 0d;

        if (!active || !prodActive) {
            // the production cost is 0 if the production stream is not active
            return;
        }

        productionCost = production.getCost();

        for (BomComponent component : bomStreamMap.keySet()) {
            SupplyChain chain = bomStreamMap.get(component);
            chain.calcProductionCost(visited);

            productionCost += chain.getProductionCost()*component.getQuantity();
        }
    }

    /**
     * Calculate the transit cost for providing one item by the supply chain.
     * For now, it is simply arbitrarily choosing one source plant to transit in.
     */
    public void calcTransitCost(Set<SupplyChain> visited) {
        if (visited.contains(this))
            return;

        visited.add(this);

        transitCost = 0d;

        if (!active)
            return;

        for (Transit transit : transitStreamMap.keySet()) {
            SupplyChain chain = transitStreamMap.get(transit);

            if (chain.length > length)
                continue;

            if (chain.isActive()) {
                chain.calcTransitCost(visited);
                transitCost = chain.getTransitCost()+transit.getCost();
                return;
            }
        }
    }

    /**
     * Add the supply chain that provide a quantity of item to the schedule.
     * It first check direct supply, then production stream, then transit streams.
     * @param dateId the date id to provide the item.
     * @param quantity the quantity of the item provided.
     * @param schedule the schedule.
     */
    public void addToSchedule(int dateId, double quantity, Schedule schedule) {
//        System.out.println("add supply chain " + toString() + " in day " + dateId);
        double left = quantity;
        double provideQuantity;

        // first try direct supply from inventory
        if (inventory > 0) {
            provideQuantity = inventory;

            if (provideQuantity > left)
                provideQuantity = left;

            left -= provideQuantity;
        }

        if (left == 0)
            return;

        // then try production stream
        if (prodActive) {
            provideQuantity = maxProdQuantity;

            if (provideQuantity > left)
                provideQuantity = left;

            // calculate the production lots and add it to the schedule
            long lots = production.lots(provideQuantity, maxProdQuantity);
            long prodQuantity = lots*production.getLotSize();
            int prodStartDateId = dateId-production.getLeadTime();

            // add all the bom streams to the schedule
            for (BomComponent component : bomStreamMap.keySet()) {
                SupplyChain chain = bomStreamMap.get(component);
                double chainQuantity = prodQuantity*component.getQuantity();

                chain.addToSchedule(prodStartDateId, chainQuantity, schedule);
            }

            schedule.addProduction(prodStartDateId, production, lots);

            left -= provideQuantity;
        }

        if (left == 0)
            return;

        while (left > 0) {
            // try the active transit streams
            for (Transit transit : transitStreamMap.keySet()) {
                SupplyChain chain = transitStreamMap.get(transit);

                if (!chain.isActive())
                    continue;

                provideQuantity = chain.getMaxQuantity();

                if (provideQuantity > left)
                    provideQuantity = left;

                int transitStartTimeId = dateId-transit.getLeadTime();
                chain.addToSchedule(transitStartTimeId, provideQuantity, schedule);
                schedule.addTransit(transitStartTimeId, transit, provideQuantity);

                left -= provideQuantity;

                if (left == 0)
                    return;
            }
        }
    }

    public SupplyChain cloneActive() {
        SupplyChain cloned = new SupplyChain(item, plant);
        cloned.setDateId(dateId);
        cloned.setActive(active);
        cloned.setProdActive(prodActive);
        cloned.setMaxQuantity(maxQuantity);
        cloned.setMaxProdQuantity(maxProdQuantity);
        cloned.setInventory(inventory);

        if (prodActive) {
            cloned.setProduction(production);
            for (BomComponent component : bomStreamMap.keySet()) {
                SupplyChain clonedBomStream = bomStreamMap.get(component).cloneActive();
                cloned.getBomStreamMap().put(component, clonedBomStream);
            }
        }

        for (Transit transit : transitStreamMap.keySet()) {
            SupplyChain transitStream = transitStreamMap.get(transit);

            if (transitStream.length > length) // to avoid loop
                continue;

            if (transitStream.isActive()) {
                cloned.getTransitStreamMap().put(transit, transitStream.cloneActive());
            }
        }

        cloned.setLeadTime(leadTime);
        cloned.setHoldingCost(holdingCost);
        cloned.setProductionCost(productionCost);
        cloned.setTransitCost(transitCost);
        cloned.setTotalCost(totalCost);
        cloned.setLength(length);

        return cloned;
    }

    @Override
    public String toString() {
        return "{" + item.toString() + ", " + plant.toString() + "}";
    }

    @Override
    public int compareTo(SupplyChain o) {
        // first compare the max quantity
        if (maxQuantity > o.maxQuantity)
            return -1;

        if (maxQuantity < o.maxQuantity)
            return 1;

        // then compare the plant
        return plant.compareTo(o.plant);
    }
}
