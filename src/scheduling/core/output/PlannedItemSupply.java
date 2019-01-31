package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;

import java.util.HashMap;
import java.util.Map;

public class PlannedItemSupply {
    private Item item;
    private Map<Plant, PlannedSupply> supplyMap;

    public PlannedItemSupply(Item item, Map<Plant, PlannedSupply> supplyMap) {
        this.item = item;
        this.supplyMap = supplyMap;
    }

    public PlannedItemSupply(Item item) {
        this(item, new HashMap<>());
    }

    public Item getItem() {
        return item;
    }

    public Map<Plant, PlannedSupply> getSupplyMap() {
        return supplyMap;
    }

    public PlannedSupply getSupplyForPlant(Plant plant) {
        return supplyMap.get(plant);
    }

    public void addPlannedSupply(PlannedSupply plannedSupply) {
        Plant plant = plannedSupply.getPlant();

        PlannedSupply oldPlannedSupply = supplyMap.get(plant);

        if (oldPlannedSupply == null) {
            supplyMap.put(plant, plannedSupply);
        }
        else {
            oldPlannedSupply.addQuantity(plannedSupply.getQuantity());
        }
    }

    public void removePlannedSupply(PlannedSupply plannedSupply) {
        Plant plant = plannedSupply.getPlant();

        PlannedSupply oldPlannedSupply = supplyMap.get(plant);

        oldPlannedSupply.removeQuantity(plannedSupply.getQuantity());
    }
}
