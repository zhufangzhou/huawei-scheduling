package scheduling.scheduler;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;
import scheduling.core.input.Demand;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.simulation.PriorityRule;
import scheduling.simulation.State;
import scheduling.simulation.TieBreaker;
import scheduling.simulation.rule.ShortestChainFirst;
import scheduling.simulation.rule.UrgentChainFirst;
import scheduling.simulation.rule.UrgentDemandFirst;
import scheduling.simulation.tiebreaker.SimpleTieBreaker;

import java.io.File;
import java.util.*;

public class Rescheduler extends Scheduler {
    private Comparator<SupplyChain> chainRanker = new UrgentChainFirst();
    private Comparator<Demand> demandRanker = new UrgentDemandFirst();
    private PriorityRule<SupplyChain> chainRule = new ShortestChainFirst();
    private TieBreaker<SupplyChain> chainTB = new SimpleTieBreaker<>();

    @Override
    public void planSchedule(State state) {
        Schedule adjustedSchedule = new Schedule();
        adjustedSchedule.initWithState(state);

        // group the demands based on items
        Map<Item, List<Demand>> itemDemandMap = new HashMap<>();
        for (Demand demand : state.getDemands()) {
            Item item = demand.getItem();

            if (itemDemandMap.containsKey(item)) {
                itemDemandMap.get(item).add(demand);
            } else {
                itemDemandMap.put(item, new LinkedList<>());
            }
        }

        // sort the supply chains of the preplanned schedule
        Map<SupplyChain, Double> preplannedChainMap = state.getPlannedSchedule().getSupplyChainMap();
        List<SupplyChain> sortedChains = new ArrayList<>(preplannedChainMap.keySet());
        Collections.sort(sortedChains, chainRanker);

        for (SupplyChain chain : sortedChains) {
            double plannedQuantity = preplannedChainMap.get(chain);

            // reactivate the chain under the new state
            chain.reactivate(adjustedSchedule, state);

            if (!chain.isActive())
                continue;

            // add the chain into the schedule
            int dateId = chain.getDateId();
            Item item = chain.getItem();
            Plant plant = chain.getPlant();
            double left = chain.getMaxQuantity();

            // get all the demands to be supplied by the supply chain
            List<Demand> demToSupply = new ArrayList<>();
            for (Demand demand : itemDemandMap.get(item)) {
                // do not consider the future demand
                if (demand.getDateId() > dateId)
                    continue;

                demToSupply.add(demand);
            }

            // sort these demands
            Collections.sort(demToSupply, demandRanker);

            // try to supply each demand one by one by the supply chain
            for (Demand demand : demToSupply) {
                double suppQuantity = demand.getQuantity();
                if (suppQuantity > left)
                    suppQuantity = left;

                demand.supplied(chain, suppQuantity, adjustedSchedule);

                left -= suppQuantity;

                if (left == 0)
                    break;
            }

            double totalSupplied = chain.getMaxQuantity()-left;
            chain.addToSchedule(dateId, totalSupplied, adjustedSchedule);
            adjustedSchedule.getSupplyChainMap().put(chain.cloneActive(), totalSupplied);
        }

        // remove the demands which have been fully supplied
        List<Demand> demands = state.getDemands();
        List<Demand> suppliedDem = new ArrayList<>();
        for (Demand demand : demands) {
            if (demand.getQuantity() == 0)
                suppliedDem.add(demand);
        }
        demands.removeAll(suppliedDem);

        // try to add more supply chains
        Collections.sort(demands, demandRanker);

        for (Demand dem : demands) {
            supplyDemand(dem, adjustedSchedule, state, chainRule, chainTB);
        }

        state.setPlannedSchedule(adjustedSchedule);
    }

    public static void main(String[] args) {
        long start, finish, duration;

        File file = new File("data/e_vuw_test_multi_plant_11.xlsx");

        State staticProb = State.staticProbFromFile(file);

        Scheduler scheduler = new GreedyStaticScheduler();
        start = System.currentTimeMillis();
        scheduler.planSchedule(staticProb);
        finish = System.currentTimeMillis();
        duration = (finish-start);

        staticProb.getPlannedSchedule().calcFillRate();

        System.out.println("fill rate = " + staticProb.getPlannedSchedule().getFillRate());
        System.out.println("holding cost = " + staticProb.getPlannedSchedule().getHoldingCost());
        System.out.println("production cost = " + staticProb.getPlannedSchedule().getProductionCost());
        System.out.println("transit cost = " + staticProb.getPlannedSchedule().getTransitCost());
        System.out.println("finished, duration = " + duration);

        // test the same file
        State newProb = State.staticProbFromFile(file);

//        System.out.println("fill rate = " + newProb.getPlannedSchedule().getFillRate());
//        System.out.println("holding cost = " + newProb.getPlannedSchedule().getHoldingCost());
//        System.out.println("production cost = " + newProb.getPlannedSchedule().getProductionCost());
//        System.out.println("transit cost = " + newProb.getPlannedSchedule().getTransitCost());


        Scheduler rescheduler = new Rescheduler();
        start = System.currentTimeMillis();
        rescheduler.planSchedule(newProb);
        finish = System.currentTimeMillis();
        duration = (finish-start);

        newProb.getPlannedSchedule().calcFillRate();

        System.out.println("fill rate = " + newProb.getPlannedSchedule().getFillRate());
        System.out.println("holding cost = " + newProb.getPlannedSchedule().getHoldingCost());
        System.out.println("production cost = " + newProb.getPlannedSchedule().getProductionCost());
        System.out.println("transit cost = " + newProb.getPlannedSchedule().getTransitCost());
        System.out.println("finished, duration = " + duration);
    }
}
