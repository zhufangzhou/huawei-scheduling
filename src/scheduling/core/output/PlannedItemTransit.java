package scheduling.core.output;

import org.apache.commons.math3.util.Pair;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;

import java.util.HashMap;
import java.util.Map;

public class PlannedItemTransit {
    private Item item;
    private Map<Pair<Plant, Plant>, PlannedTransit> transitMap;

    public PlannedItemTransit(Item item, Map<Pair<Plant, Plant>, PlannedTransit> transitMap) {
        this.item = item;
        this.transitMap = transitMap;
    }

    public PlannedItemTransit(Item item) {
        this(item, new HashMap<>());
    }

    public Item getItem() {
        return item;
    }

    public Map<Pair<Plant, Plant>, PlannedTransit> getTransitMap() {
        return transitMap;
    }

    public PlannedTransit getTransitForPlants(Plant fromPlant, Plant toPlant) {
        return transitMap.get(new Pair<>(fromPlant, toPlant));
    }

    public void addPlannedTransit(PlannedTransit plannedTransit) {
        Plant fromPlant = plannedTransit.getFromPlant();
        Plant toPlant = plannedTransit.getToPlant();

        Pair<Plant, Plant> plantPair = new Pair<>(fromPlant, toPlant);

        PlannedTransit oldPlannedTransit = transitMap.get(plantPair);

        if (oldPlannedTransit == null) {
            transitMap.put(plantPair, plannedTransit);
        }
        else {
            oldPlannedTransit.addQuantity(plannedTransit.getQuantity());
        }
    }

    public void removePlannedTransit(PlannedTransit plannedTransit) {
        Plant fromPlant = plannedTransit.getFromPlant();
        Plant toPlant = plannedTransit.getToPlant();

        Pair<Plant, Plant> plantPair = new Pair<>(fromPlant, toPlant);

        PlannedTransit oldPlannedTransit = transitMap.get(plantPair);

        oldPlannedTransit.removeQuantity(plannedTransit.getQuantity());
    }
}
