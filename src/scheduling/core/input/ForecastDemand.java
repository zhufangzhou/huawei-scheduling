package scheduling.core.input;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;

public class ForecastDemand extends Demand {
    public ForecastDemand(int dateId, Item item, long quantity) {
        super(dateId, item, quantity);
        this.type = 0;
        this.priority = type+dateId;
    }

    @Override
    public int latestSupplyDate(Schedule schedule) {
        return dateId+1;
    }

    @Override
    public void supplied(SupplyChain supplyChain, long suppQuantity, Schedule schedule) {
        int dateId = supplyChain.getDateId();
        Item item = supplyChain.getItem();
        Plant plant = supplyChain.getPlant();

        Pair<Item, Plant> supply = new Pair<>(item, plant);
        schedule.addForecastSupply(dateId, supply, suppQuantity);

        quantity -= suppQuantity;
    }

    @Override
    public String toString() {
        return "FD[" + dateId + ", " + item.toString() + ", " + quantity + "]";
    }
}
