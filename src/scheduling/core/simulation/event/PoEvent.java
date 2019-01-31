package scheduling.core.simulation.event;

import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.simulation.Event;
import scheduling.core.simulation.Simulator;

import java.util.HashMap;
import java.util.Map;

public class PoEvent extends Event {
    private Item item;
    private Plant plant;
    private int quantity;

    public PoEvent(int date, Item item, Plant plant, int quantity) {
        super(date);
        this.item = item;
        this.plant = plant;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public Plant getPlant() {
        return plant;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Buy some items at a plant.
     * (1) Increase the inventory of the item at the plant.
     * @param simulator the simulator.
     */
    @Override
    public void trigger(Simulator simulator) {
        Map<Item, Map<Plant, Integer>> inventoryMap = simulator.getState().getInventoryMap();
        Map<Plant, Integer> itemInvMap = inventoryMap.get(item);

        if (itemInvMap == null) {
            itemInvMap = new HashMap<>();
            inventoryMap.put(item, itemInvMap);
        }

        int oldInv = 0;
        if (itemInvMap.containsKey(plant))
            oldInv = itemInvMap.get(plant);
        itemInvMap.put(plant, oldInv+quantity);
    }

    @Override
    public String toString() {
        return "<Po: " + item.toString() + ", " + plant.toString() + ", " + quantity + ">";
    }
}
