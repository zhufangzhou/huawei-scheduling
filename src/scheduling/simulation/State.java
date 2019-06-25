package scheduling.simulation;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Environment;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;
import scheduling.core.input.*;
import scheduling.scheduler.GreedyStaticScheduler;
import scheduling.scheduler.Scheduler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The current state of the scheduler.
 * In the static scheduling environment, it is used to represent the static problem.
 * In the dynamic scheduling environment, it is a state during the simulation.
 */

public class State {
    private Environment env; // the environment

    private int dateIndex; // the current date index

    private Map<Integer, List<Item>> orderDemMap; // the daily ordered demands (items)
    private Map<Integer, List<Item>> forecastDemMap; // the daily forecast demands (items)

    private Schedule plannedSchedule; // the planned schedule that is not executed yet.
    private Schedule executedSchedule; // the executed schedule so far (during the simulation)

    private Map<Item, Map<Plant, SupplyChain>> supplyChainMap; // the supply chain of each item at each plant

    public State(Environment env, Schedule plannedSchedule, Schedule executedSchedule) {
        this.env = env;
        this.dateIndex = 0;
        this.plannedSchedule = plannedSchedule;
        this.executedSchedule = executedSchedule;

        initDemMaps();
        initSupplyChainMap();
    }

    /**
     * The default plannedSchedule is an empty schedule.
     * @param env the environment.
     */
    public State(Environment env) {
        this.env = env;
        this.dateIndex = 0;
        plannedSchedule = new Schedule();
        executedSchedule = new Schedule();

        initDemMaps();
        initSupplyChainMap();

        plannedSchedule.initWithState(this);
    }

    /**
     * Read a static problem from a file and the start date.
     * @param file the file.
     * @param columnNameReader column name mapping json file.
     * @return the state.
     */
    public static State staticProbFromFile(File file, FileReader columnNameReader) {
        Environment env = Environment.readFromFile(file, columnNameReader);

        return new State(env);
    }

    public Environment getEnv() {
        return env;
    }

    public int getDateIndex() {
        return dateIndex;
    }

    public Schedule getPlannedSchedule() {
        return plannedSchedule;
    }

    public Schedule getExecutedSchedule() {
        return executedSchedule;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setDateIndex(int dateIndex) {
        this.dateIndex = dateIndex;
    }

    public void setPlannedSchedule(Schedule plannedSchedule) {
        this.plannedSchedule = plannedSchedule;
    }

    public void setExecutedSchedule(Schedule executedSchedule) {
        this.executedSchedule = executedSchedule;
    }

    public Map<Integer, List<Item>> getOrderDemMap() {
        return orderDemMap;
    }

    public void setOrderDemMap(Map<Integer, List<Item>> orderDemMap) {
        this.orderDemMap = orderDemMap;
    }

    public Map<Integer, List<Item>> getForecastDemMap() {
        return forecastDemMap;
    }

    public void setForecastDemMap(Map<Integer, List<Item>> forecastDemMap) {
        this.forecastDemMap = forecastDemMap;
    }

    public Map<Item, Map<Plant, SupplyChain>> getSupplyChainMap() {
        return supplyChainMap;
    }

    public void setSupplyChainMap(Map<Item, Map<Plant, SupplyChain>> supplyChainMap) {
        this.supplyChainMap = supplyChainMap;
    }

    /**
     * Initialise the order demand map and forecast demand map.
     */
    public void initDemMaps() {
        orderDemMap = new HashMap<>();
        forecastDemMap = new HashMap<>();

        for (Item item : env.getItemMap().values()) {
            for (int dateId : item.getOrderDemandMap().keySet()) {
                List<Item> dailyDem = orderDemMap.get(dateId);
                if (dailyDem == null) {
                    dailyDem = new ArrayList<>();
                    orderDemMap.put(dateId, dailyDem);
                }

                dailyDem.add(item);
            }

            for (int dateId : item.getForecastDemandMap().keySet()) {
                List<Item> dailyDem = forecastDemMap.get(dateId);
                if (dailyDem == null) {
                    dailyDem = new ArrayList<>();
                    forecastDemMap.put(dateId, dailyDem);
                }

                dailyDem.add(item);
            }
        }
    }

    public void initSupplyChainMap() {
        supplyChainMap = new HashMap<>();

        for (Item item : env.getItemMap().values()) {
            Map<Plant, SupplyChain> map = new HashMap<>();

//            System.out.println("initialise chain for " + item.toString());

            for (Plant plant : item.getPlants()) {
//                System.out.println("initialising chain [" + item.toString() + ", " + plant.toString() + "]");

                map.put(plant, new SupplyChain(item, plant));
            }

            supplyChainMap.put(item, map);
        }

        // link the supply chains through bom streams
        for (Item item : env.getItemMap().values()) {
            for (Plant plant : item.getPlants()) {
                SupplyChain targetChain = supplyChainMap.get(item).get(plant);

                Production production = targetChain.getProduction();

                if (production != null) {
                    for (BomComponent component : production.getBom()) {
                        Item material = component.getMaterial();
                        SupplyChain chain = supplyChainMap.get(material).get(plant);
                        targetChain.getBomStreamMap().put(component, chain);
                    }
                }
            }
        }

        // link the supply chains through transit streams
        for (Item item : env.getItemMap().values()) {
            for (Plant plant : item.getPlants()) {
                SupplyChain targetChain = supplyChainMap.get(item).get(plant);

//                System.out.println("target chain [" + item.toString() + ", " + plant.toString() + "]");

                for (Plant fromPlant : plant.getTransitInMap().keySet()) {
                    // check whether one can transit the item between the plants
                    if (!plant.getTransitInMap().get(fromPlant).contains(item))
                        continue;

                    Pair<Plant, Plant> pair = new Pair<>(fromPlant, plant);
                    Transit transit = item.getTransitMap().get(pair);
                    SupplyChain sourceChain = supplyChainMap.get(item).get(fromPlant);

//                    System.out.println(item.toString() + ", " + plant.toString() + " <- " + fromPlant.toString());

                    targetChain.getTransitStreamMap().put(transit, sourceChain);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("data/e_vuw_test_multi_plant_03.xlsx");
        FileReader columnNameReader = new FileReader("conf/column_name_v1.json");

        State staticProb = State.staticProbFromFile(file, columnNameReader);

        Scheduler scheduler = new GreedyStaticScheduler();
        long start = System.currentTimeMillis();
        scheduler.planSchedule(staticProb);
        long finish = System.currentTimeMillis();
        long duration = (finish-start);

        staticProb.getPlannedSchedule().calcFillRate();
        System.out.println("fill rate = " + staticProb.getPlannedSchedule().getFillRate());
        System.out.println("holding cost = " + staticProb.getPlannedSchedule().getHoldingCost());
        System.out.println("production cost = " + staticProb.getPlannedSchedule().getProductionCost());
        System.out.println("transit cost = " + staticProb.getPlannedSchedule().getTransitCost());
        System.out.println("finished, duration = " + duration);
    }
}
