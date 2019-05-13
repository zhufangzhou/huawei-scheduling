package scheduling.core.output;

import org.apache.commons.math3.util.Pair;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;

public class SupplyInstruction extends Instruction {
    private Pair<Item, Plant> supply;
    private long quantity;

    public SupplyInstruction(int startDate, int endDate, Item item, Pair<Item, Plant> supply, long quantity) {
        super(startDate, endDate, item);
        this.supply = supply;
        this.quantity = quantity;
    }

    public Pair<Item, Plant> getSupply() {
        return supply;
    }

    public void setSupply(Pair<Item, Plant> supply) {
        this.supply = supply;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "[" + supply.toString() + ": " + quantity + "]";
    }
}
