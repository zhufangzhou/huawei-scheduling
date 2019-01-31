package scheduling.core.output;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;

import java.util.HashMap;
import java.util.Map;

public class DailyPlan {
    private Map<Item, PlannedItemProduction> plannedItemProductionMap;
    private Map<Plant, PlannedPlantProduction> plannedPlantProductionMap;
    private Map<Item, PlannedItemTransit> plannedItemTransitMap;
    private Map<Item, PlannedItemSupply> plannedItemSupplyMap;

    public DailyPlan(Map<Item, PlannedItemProduction> plannedItemProductionMap, Map<Plant, PlannedPlantProduction> plannedPlantProductionMap, Map<Item, PlannedItemTransit> plannedItemTransitMap, Map<Item, PlannedItemSupply> plannedItemSupplyMap) {
        this.plannedItemProductionMap = plannedItemProductionMap;
        this.plannedPlantProductionMap = plannedPlantProductionMap;
        this.plannedItemTransitMap = plannedItemTransitMap;
        this.plannedItemSupplyMap = plannedItemSupplyMap;
    }

    public DailyPlan() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public Map<Item, PlannedItemProduction> getPlannedItemProductionMap() {
        return plannedItemProductionMap;
    }

    public Map<Plant, PlannedPlantProduction> getPlannedPlantProductionMap() {
        return plannedPlantProductionMap;
    }

    public Map<Item, PlannedItemTransit> getPlannedItemTransitMap() {
        return plannedItemTransitMap;
    }

    public Map<Item, PlannedItemSupply> getPlannedItemSupplyMap() {
        return plannedItemSupplyMap;
    }

    public void addPlannedProduction(PlannedProduction plannedProduction) {
        Item item = plannedProduction.getProduction().getItem();
        Plant plant = plannedProduction.getProduction().getPlant();

        PlannedItemProduction plannedItemProduction = plannedItemProductionMap.get(item);
        if (plannedItemProduction == null) {
            plannedItemProduction = new PlannedItemProduction(item);
        }
        plannedItemProduction.addPlannedProduction(plannedProduction);
        plannedItemProductionMap.put(item, plannedItemProduction);

        PlannedPlantProduction plannedPlantProduction = plannedPlantProductionMap.get(plant);
        if (plannedPlantProduction == null) {
            plannedPlantProduction = new PlannedPlantProduction(plant);
        }
        plannedPlantProduction.addPlannedProduction(plannedProduction);
        plannedPlantProductionMap.put(plant, plannedPlantProduction);
    }

    public void removePlannedProduction(PlannedProduction plannedProduction) {
        Item item = plannedProduction.getProduction().getItem();
        Plant plant = plannedProduction.getProduction().getPlant();

        PlannedItemProduction plannedItemProduction = plannedItemProductionMap.get(item);
        plannedItemProduction.removePlannedProduction(plannedProduction);

        PlannedPlantProduction plannedPlantProduction = plannedPlantProductionMap.get(plant);
        plannedPlantProduction.removePlannedProduction(plannedProduction);
    }

    public void addPlannedTransit(PlannedTransit plannedTransit) {
        Item item = plannedTransit.getItem();

        PlannedItemTransit plannedItemTransit = plannedItemTransitMap.get(item);

        if (plannedItemTransit == null) {
            plannedItemTransit = new PlannedItemTransit(item);
        }
        plannedItemTransit.addPlannedTransit(plannedTransit);
        plannedItemTransitMap.put(item, plannedItemTransit);
    }

    public void removePlannedTransit(PlannedTransit plannedTransit) {
        Item item = plannedTransit.getItem();

        PlannedItemTransit plannedItemTransit = plannedItemTransitMap.get(item);
        plannedItemTransit.removePlannedTransit(plannedTransit);
    }
}
