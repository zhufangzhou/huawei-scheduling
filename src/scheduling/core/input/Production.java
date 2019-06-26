package scheduling.core.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A production of an item.
 */

public class Production {
    private Item item;
    private Plant plant;
    private double cost;
    private int leadTime;
    private int prevWeekProduction;
    private int weekToDateProduction;
    private int lotSize;
    private int minProduction;
    private int maxProduction;
    private int fixedDaysSupply;
    private List<BomComponent> bom;
    private Map<MachineSet, Double> rateMap; // the rate (capacity consumption) of the production

    public Production(Item item, Plant plant, double cost, int leadTime, int prevWeekProduction, int weekToDateProduction, int lotSize, int minProduction, int maxProduction, int fixedDaysSupply) {
        this.item = item;
        this.plant = plant;
        this.cost = cost;
        this.leadTime = leadTime;
        this.prevWeekProduction = prevWeekProduction;
        this.weekToDateProduction = weekToDateProduction;
        this.lotSize = lotSize;
        this.minProduction = minProduction;
        this.maxProduction = maxProduction;
        this.fixedDaysSupply = fixedDaysSupply;

        bom = new ArrayList<>();
    }

    public Item getItem() {
        return item;
    }

    public Plant getPlant() {
        return plant;
    }

    public double getCost() {
        return cost;
    }

    public int getLeadTime() {
        return leadTime;
    }

    public int getPrevWeekProduction() {
        return prevWeekProduction;
    }

    public int getWeekToDateProduction() {
        return weekToDateProduction;
    }

    public int getLotSize() {
        return lotSize;
    }

    public int getMinProduction() {
        return minProduction;
    }

    public int getMaxProduction() {
        return maxProduction;
    }

    public int getFixedDaysSupply() {
        return fixedDaysSupply;
    }

    public List<BomComponent> getBom() {
        return bom;
    }

    public Map<MachineSet, Double> getRateMap() {
        return rateMap;
    }

    public void addBom(BomComponent bomComponent) {
        bom.add(bomComponent);
    }

    public void calcRateMap() {
        rateMap = new HashMap<>();

        List<MachineSet> machineSets = item.getMachineMap().get(plant);

        for (MachineSet machineSet : machineSets) {
            CapacityType capacityType = machineSet.getCapacityType();
            double rate = item.getRateMap().get(capacityType);

            rateMap.put(machineSet, rate);
        }
    }

    /**
     * Calculate the max quantity of the production based on the machines' capacity.
     * @param dateId the date to start the production.
     * @return the max quantity of the production based on the machines' capacity.
     */
    public long maxQuantityFromCapacity(int dateId) {
        long maxQuantity = Long.MAX_VALUE;

        List<MachineSet> machineSets = item.getMachineMap().get(plant);

        for (MachineSet machineSet : machineSets) {
            double rate = rateMap.get(machineSet);
            long quantity;

            if (rate == 0) {
                quantity = Long.MAX_VALUE;
            } else {
                double remCap = 0;
                if (machineSet.getCapacityMap().containsKey(dateId))
                    remCap = machineSet.getCapacityMap().get(dateId).getRemaining();

                quantity = (long)(remCap/rate);
            }

            if (maxQuantity > quantity)
                maxQuantity = quantity;
        }

        return maxQuantity;
    }

    /**
     * Calculate the number of lots to provide at least quantity,
     * but no greater than a max quantity.
     * @param quantity the quantity to be provided.
     * @param maxQuantity the max quantity.
     * @return the number of lots.
     */
    public long lots(double quantity, long maxQuantity) {
        // lots1 is the minimum lots to provide at least quantity
        long lots = (long)(Math.ceil(quantity/lotSize));

        if (lots > maxQuantity/lotSize)
            lots--;

        return lots;
    }

    @Override
    public String toString() {
        return "Production{" +
                "" + item.toString() +
                ", " + plant.toString() +
                '}';
    }
}
