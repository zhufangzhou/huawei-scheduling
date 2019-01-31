package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.input.Production;

import java.util.HashMap;
import java.util.Map;

public class PlannedItemProduction {
    private Item item;
    private Map<Plant, PlannedProduction> productionMap;

    public PlannedItemProduction(Item item, Map<Plant, PlannedProduction> productionMap) {
        this.item = item;
        this.productionMap = productionMap;
    }

    public PlannedItemProduction(Item item) {
        this.item = item;

        productionMap = new HashMap<>();
        for (Production production : item.getProductionMap().values()) {
            Plant plant = production.getPlant();

        }
    }

    public Item getItem() {
        return item;
    }

    public Map<Plant, PlannedProduction> getProductionMap() {
        return productionMap;
    }

    public PlannedProduction getProductionForPlant(Plant plant) {
        return productionMap.get(plant);
    }

    public void addPlannedProduction(PlannedProduction plannedProduction) {
        Plant plant = plannedProduction.getProduction().getPlant();

        PlannedProduction oldPlannedProduction = productionMap.get(plant);

        if (oldPlannedProduction == null) {
            productionMap.put(plant, plannedProduction);
        }
        else {
            oldPlannedProduction.addLots(plannedProduction.getLots());
        }
    }

    public void removePlannedProduction(PlannedProduction plannedProduction) {
        Plant plant = plannedProduction.getProduction().getPlant();

        PlannedProduction oldPlannedProduction = productionMap.get(plant);

        oldPlannedProduction.removeLots(plannedProduction.getLots());
    }

    public int producedQuantity() {
        int quantity = 0;
        for (PlannedProduction plannedProduction : productionMap.values()) {
            quantity += plannedProduction.getQuantity();
        }

        return quantity;
    }
}
