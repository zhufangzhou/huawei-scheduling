package scheduling.simulation;

import io.ExcelProcessor;
import org.apache.commons.math3.util.Pair;
import scheduling.core.Environment;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;
import scheduling.core.input.*;
import scheduling.scheduler.GreedyStaticScheduler;
import scheduling.scheduler.Scheduler;

import java.io.File;
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

    public State(Environment env, Schedule plannedSchedule, Schedule executedSchedule) {
        this.env = env;
        this.dateIndex = 0;
        this.plannedSchedule = plannedSchedule;
        this.executedSchedule = executedSchedule;

        initDemMaps();
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

        plannedSchedule.initWithState(this);
    }

    /**
     * Read a static problem from a file and the start date.
     * @param file the file.
     * @return the state.
     */
    public static State staticProbFromFile(File file) {
        Environment env = Environment.readFromFile(file);

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

    public static void main(String[] args) {
        File file = new File("data/e_vuw_test_multi_plant_01.xlsx");

        State staticProb = State.staticProbFromFile(file);

        Scheduler scheduler = new GreedyStaticScheduler();
        long start = System.currentTimeMillis();
        scheduler.planSchedule(staticProb);
        long finish = System.currentTimeMillis();
        long duration = (finish-start);

        staticProb.getPlannedSchedule().calcFillRate();

//        File outputFile = new File("output.xlsx");
//        ExcelProcessor.outputSchedule(staticProb.getPlannedSchedule(), outputFile, staticProb);
        System.out.println("fill rate = " + staticProb.getPlannedSchedule().getFillRate());
        System.out.println("holding cost = " + staticProb.getPlannedSchedule().getHoldingCost());
        System.out.println("production cost = " + staticProb.getPlannedSchedule().getProductionCost());
        System.out.println("transit cost = " + staticProb.getPlannedSchedule().getTransitCost());
        System.out.println("finished, duration = " + duration);
    }
}
