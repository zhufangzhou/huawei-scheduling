package scheduling.core.input;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;

public class OrderDemand extends Demand {
    public OrderDemand(int dateId, Item item, long quantity) {
        super(dateId, item, quantity);
    }

    @Override
    public void supplied(SupplyChain supplyChain, long suppQuantity, Schedule schedule) {
        int dateId = supplyChain.getDateId();
        Item item = supplyChain.getItem();
        Plant plant = supplyChain.getPlant();

        Pair<Item, Plant> supply = new Pair<>(item, plant);
        schedule.addOrderSupply(dateId, supply, suppQuantity);

        quantity -= suppQuantity;
    }

    @Override
    public String toString() {
        return "OD[" + dateId + ", " + item.toString() + ", " + quantity + "]";
    }
}
