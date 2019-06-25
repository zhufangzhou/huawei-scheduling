package scheduling.core.input;

import java.util.*;

/**
 * A plant/factory.
 */

public class Plant implements Comparable<Plant> {
    private String name;
    private int lockedOutDays;
    private String type;
    private Map<Plant, List<Item>> transitOutMap;
    private Map<Plant, List<Item>> transitInMap;
    private Map<Integer, Map<Item, Long>> workInProcessMap;
    private Map<String, MachineSet> machineSetMap;
    private Map<ItemType, Integer> frozenDaysMap;
    private List<Integer> holidays;
    private Map<Integer, Map<Item, Long>> rawMaterialPoMap;
    // items that can be stored in this plant
    private Set<Item> itemSet;

    public Plant(String name, int lockedOutDays, String type) {
        this.name = name;
        this.type = type;
        this.lockedOutDays = lockedOutDays;

        this.transitOutMap = new HashMap<>();
        this.transitInMap = new HashMap<>();
        this.workInProcessMap = new HashMap<>();
        this.machineSetMap = new HashMap<>();
        this.frozenDaysMap = new HashMap<>();
        this.holidays = new ArrayList<>();
        this.rawMaterialPoMap = new HashMap<>();
        this.itemSet = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Set<Item> getItemSet() {
        return itemSet;
    }

    public void setItemSet(Set<Item> itemSet) {
        this.itemSet = itemSet;
    }

    public int getLockedOutDays() {
        return lockedOutDays;
    }

    public Map<Plant, List<Item>> getTransitOutMap() {
        return transitOutMap;
    }

    public Map<Plant, List<Item>> getTransitInMap() {
        return transitInMap;
    }

    public Map<Integer, Map<Item, Long>> getWorkInProcessMap() {
        return workInProcessMap;
    }

    public Map<String, MachineSet> getMachineSetMap() {
        return machineSetMap;
    }

    public Map<ItemType, Integer> getFrozenDaysMap() {
        return frozenDaysMap;
    }

    public List<Integer> getHolidays() {
        return holidays;
    }

    public Map<Integer, Map<Item, Long>> getRawMaterialPoMap() {
        return rawMaterialPoMap;
    }

    public void putTransitOutMap(Plant plant, Item item) {
        List<Item> value = transitOutMap.get(plant);
        if (value == null)
            value = new ArrayList<>();
        value.add(item);

        transitOutMap.put(plant, value);
    }

    public void putTransitInMap(Plant plant, Item item) {
        List<Item> value = transitInMap.get(plant);
        if (value == null)
            value = new ArrayList<>();
        value.add(item);

        transitInMap.put(plant, value);
    }

    public void putWorkInProcess(Item item, int date, long quantity) {
        Map<Item, Long> dailyWIP = workInProcessMap.get(date);
        if (dailyWIP == null) {
            dailyWIP = new HashMap<>();
            workInProcessMap.put(date, dailyWIP);
        }
        dailyWIP.put(item, quantity);
    }

    public void putMachineSet(MachineSet machineSet) {
        machineSetMap.put(machineSet.getName(), machineSet);
    }

    public void putRawMaterialPo(Item item, long quantity, int date) {
        Map<Item, Long> dailyRawMatPo = rawMaterialPoMap.get(date);

        if (dailyRawMatPo == null) {
            dailyRawMatPo = new HashMap<>();
            rawMaterialPoMap.put(date, dailyRawMatPo);
        }
        dailyRawMatPo.put(item, quantity);
    }

    public void putItem(Item item) {
        this.itemSet.add(item);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Plant o) {
        return name.compareTo(o.name);
    }
}
