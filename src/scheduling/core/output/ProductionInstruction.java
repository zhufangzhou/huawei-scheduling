package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Production;

public class ProductionInstruction extends Instruction {
    private Production production;
    private long lots;
    private long quantity;

    public ProductionInstruction(int startDate, int endDate, Item item, Production production, long lots) {
        super(startDate, endDate, item);
        this.production = production;
        this.lots = lots;
        this.quantity = lots*production.getLotSize();
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public long getLots() {
        return lots;
    }

    public void setLots(long lots) {
        this.lots = lots;
        this.quantity = lots*production.getLotSize();
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "[" + production.toString() + ": " + lots + "]";
    }
}
