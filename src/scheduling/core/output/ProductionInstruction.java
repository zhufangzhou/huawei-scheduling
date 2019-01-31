package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Production;
import scheduling.core.simulation.State;

public class ProductionInstruction extends Instruction {
    private Production production;
    private int lots;

    public ProductionInstruction(int startDate, int endDate, Item item, Production production, int lots) {
        super(startDate, endDate, item);
        this.production = production;
        this.lots = lots;
    }

    public Production getProduction() {
        return production;
    }

    public int getLots() {
        return lots;
    }

    public void addLots(int addedLots) {
        this.lots += addedLots;
    }

    public void removeLots(int removedLots) {
        this.lots -= removedLots;
    }

    public int getQuantity() {
        return lots * production.getLotSize();
    }

    @Override
    public String toString() {
        return "PROD[" + production.getItem().toString() + ", " +
                production.getPlant().toString() + ", " + lots + "]";
    }
}
