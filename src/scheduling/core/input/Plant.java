package scheduling.core.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A plant/factory.
 */

public class Plant implements Comparable<Plant> {
    private String name;
    private int lockedOutDays;
    private String type;
    private Map<Plant, List<Item>> transitOutMap;
    private Map<Plant, List<Item>> transitInMap;
    private Map<Integer, Map<Item, Double>> workInProcessMap;
    private Map<String, MachineSet> machineSetMap;
    private Map<ItemType, Integer> frozenDaysMap;
    private List<Integer> holidays;
    private Map<Integer, Map<Item, Double>> rawMaterialPoMap;

    public Plant(String name, int lockedOutDays, String type) {
        this.name = name;
        this.type = type;
        this.lockedOutDays = lockedOutDays;

        transitOutMap = new HashMap<>();
        transitInMap = new HashMap<>();
        workInProcessMap = new HashMap<>();
        machineSetMap = new HashMap<>();
        frozenDaysMap = new HashMap<>();
        holidays = new ArrayList<>();
        rawMaterialPoMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
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

    public Map<Integer, Map<Item, Double>> getWorkInProcessMap() {
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

    public Map<Integer, Map<Item, Double>> getRawMaterialPoMap() {
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

    public void putWorkInProcess(Item item, int date, double quantity) {
        Map<Item, Double> dailyWIP = workInProcessMap.get(date);
        if (dailyWIP == null) {
            dailyWIP = new HashMap<>();
            workInProcessMap.put(date, dailyWIP);
        }
        dailyWIP.put(item, quantity);
    }

    public void putMachineSet(MachineSet machineSet) {
        machineSetMap.put(machineSet.getName(), machineSet);
    }

    public void putRawMaterialPo(Item item, double quantity, int date) {
        Map<Item, Double> dailyRawMatPo = rawMaterialPoMap.get(date);

        if (dailyRawMatPo == null) {
            dailyRawMatPo = new HashMap<>();
            rawMaterialPoMap.put(date, dailyRawMatPo);
        }
        dailyRawMatPo.put(item, quantity);
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
