package scheduling.core.output;

import scheduling.core.input.CapacityType;
import scheduling.core.input.Item;
import scheduling.core.input.MachineSet;
import scheduling.core.input.Plant;

import java.util.HashMap;
import java.util.Map;

public class PlannedPlantProduction {
    private Plant plant;
    private Map<Item, PlannedProduction> productionMap;

    public PlannedPlantProduction(Plant plant, Map<Item, PlannedProduction> productionMap) {
        this.plant = plant;
        this.productionMap = productionMap;
    }

    public PlannedPlantProduction(Plant plant) {
        this(plant, new HashMap<>());
    }

    public Map<Item, PlannedProduction> getProductionMap() {
        return productionMap;
    }

    public PlannedProduction getItemProduction(Item item) {
        return productionMap.get(item);
    }

    public void addPlannedProduction(PlannedProduction plannedProduction) {
        Item item = plannedProduction.getProduction().getItem();

        PlannedProduction oldPlannedProduction = productionMap.get(item);

        if (oldPlannedProduction == null) {
            productionMap.put(item, plannedProduction);
        }
        else {
            oldPlannedProduction.addLots(plannedProduction.getLots());
        }
    }

    public void removePlannedProduction(PlannedProduction plannedProduction) {
        Item item = plannedProduction.getProduction().getItem();

        PlannedProduction oldPlannedProduction = productionMap.get(item);

        oldPlannedProduction.removeLots(plannedProduction.getLots());
    }

    public Map<MachineSet, Double> consumedCapacities() {
        Map<MachineSet, Double> consumedCapacitiesMap = new HashMap<>();

        for (MachineSet machineSet : plant.getMachineSetMap().values()) {
            consumedCapacitiesMap.put(machineSet, 0d);
        }

        for (PlannedProduction plannedProduction : productionMap.values()) {
            Item item = plannedProduction.getProduction().getItem();
            Plant plant = plannedProduction.getProduction().getPlant();
            MachineSet machineSet = item.getMachineMap().get(plant);
            double rate = item.getRate();

            double consumedRate = consumedCapacitiesMap.get(machineSet);

            consumedCapacitiesMap.put(machineSet, consumedRate+rate);
        }

        return consumedCapacitiesMap;
    }
}
