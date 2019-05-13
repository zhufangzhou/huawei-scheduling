package scheduling.core.input;

import java.util.ArrayList;
import java.util.List;

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
    private List<Bom> assembly;

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

        assembly = new ArrayList<>();
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

    public List<Bom> getAssembly() {
        return assembly;
    }

    public void addBom(Bom bom) {
        assembly.add(bom);
    }

    @Override
    public String toString() {
        return "Production{" +
                "" + item.toString() +
                ", " + plant.toString() +
                '}';
    }
}
